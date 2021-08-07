package com.jespinel.noq.turns;


import com.jespinel.noq.common.exceptions.EmptyQueueException;
import com.jespinel.noq.common.exceptions.EntityNotFoundException;
import com.jespinel.noq.notifications.NotificationService;
import com.jespinel.noq.queues.Queue;
import com.jespinel.noq.queues.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static com.jespinel.noq.turns.TurnStateValue.*;


@Service
public class TurnService {

    final Logger logger = LoggerFactory.getLogger(TurnService.class);

    private final TransactionTemplate transactionTemplate;
    private final TurnRepository repository;
    private final QueueService queueService;
    private final TurnStateService turnStateService;
    private final NotificationService notificationService;
    private final TurnNumberService turnNumberService;

    @Autowired
    public TurnService(TransactionTemplate transactionTemplate,
                       TurnRepository repository,
                       QueueService queueService,
                       TurnStateService turnStateService,
                       NotificationService notificationService,
                       TurnNumberService turnNumberService) {
        this.transactionTemplate = transactionTemplate;
        this.repository = repository;
        this.queueService = queueService;
        this.turnStateService = turnStateService;
        this.notificationService = notificationService;
        this.turnNumberService = turnNumberService;
    }

    /**
     * Create a new turn in queue.
     * <p>
     * This method inserts a new row in the table turns and turn_states
     * in a single transaction.
     *
     * @param phoneNumber phone number used to take a new turn
     * @param queueId     ID of the queue where we are going to take a new turn from
     * @return A new turn in a queue.
     */
    public Turn create(String phoneNumber, long queueId) {
        Queue queue = queueService.getOrThrow(queueId);
        Turn nextTurn = generateNextTurn(phoneNumber, queue);
        Turn savedTurn = transactionTemplate.execute(createTurnTransaction(nextTurn));
        notificationService.notifyTurnCreation(savedTurn);
        return savedTurn;
    }

    /**
     * Creates a turn in a single transaction.
     *
     * @param turn The turn we want to create
     * @return A transaction callback to be used by a transactionTemplate.
     */
    private TransactionCallback<Turn> createTurnTransaction(Turn turn) {
        return transactionStatus -> {
            Turn savedTurn = repository.saveOrThrow(turn);
            turnStateService.create(savedTurn.getId());
            return savedTurn;
        };
    }

    private Turn generateNextTurn(String phoneNumber, Queue queue) {
        TurnNumber nextTurnNumber = turnNumberService.getNextTurn(queue);
        return new Turn(phoneNumber, queue.getId(), nextTurnNumber, REQUESTED);
    }

    public Optional<Turn> getPhoneNumberLatestTurn(String phoneNumber) {
        return repository.getLatestTurnByPhoneNumber(phoneNumber);
    }

    /**
     * Cancels the latest turn of the given phone number if it is in one of
     * the following states: Requested, Ready.
     * <p>
     * This method inserts a new row in the table turn_states and updates
     * the current_state of the turn in the table turns in a single transaction.
     *
     * @param phoneNumber The phone number associated to the turn we want to cancel
     * @return A Turn within an optional if the turn exists, otherwise an empty optional
     */
    public Turn cancel(String phoneNumber) {
        Optional<Turn> phoneNumberTurn = getPhoneNumberLatestTurn(phoneNumber);
        if (phoneNumberTurn.isEmpty()) {
            String errorTemplate = "%s does not have associated turns";
            String errorMessage = errorTemplate.formatted(phoneNumber);
            throw new EntityNotFoundException(errorMessage);
        }

        // Cancel turn
        Turn turn = phoneNumberTurn.get();
        Turn updatedTurn = transactionTemplate.execute(updateTurnCurrentState(turn.getId(), CANCELLED));
        notificationService.notifyCancellation(turn);

        // If the cancelled turn was in READY state, call the next turn
        if (turn.getCurrentState().equals(READY)) {
            callNextTurn(turn.getQueueId());
        }

        return updatedTurn;
    }

    /**
     * Update turn and turn_state in a single transaction
     *
     * @param turnId   The ID of the turn we want to update
     * @param newState The new state we want to assign to the turn
     * @return The updated turn
     */
    private TransactionCallback<Turn> updateTurnCurrentState(long turnId, TurnStateValue newState) {
        return transactionStatus -> {
            repository.updateTurnCurrentState(turnId, newState);
            turnStateService.moveToState(turnId, newState);
            return getOrThrow(turnId);
        };
    }

    public Turn callNextTurn(long queueId) {
        queueService.getOrThrow(queueId);
        Optional<Turn> optionalTurn = repository.getOldestRequestedTurn(queueId);
        if (optionalTurn.isEmpty()) {
            throw new EmptyQueueException("The queue is empty, there are no turns to call");
        }

        Turn nextTurn = optionalTurn.get();
        Turn updatedTurn = transactionTemplate.execute(updateTurnCurrentState(nextTurn.getId(), READY));
        notificationService.notifyReadiness(updatedTurn);
        return updatedTurn;
    }

    public Turn getOrThrow(long turnId) {
        Optional<Turn> turn = repository.findById(turnId);
        turn.orElseThrow(() -> {
            String errorMessage = "The turn with ID %s was not found".formatted(turnId);
            throw new EntityNotFoundException(errorMessage);
        });
        return turn.get();
    }

    /**
     * Move a turn from its current state to the given target state.
     * If the state transition is valid returns the turn, otherwise it throws
     * an exception.
     *
     * @param turnId The ID of the turn we want to change its state
     * @param targetState The new state we want to set in the turn
     * @return If the transition was possible returns the turn, otherwise throws an exception
     */
    public Turn updateTurn(long turnId, TurnStateValue targetState) {
        Turn turn = getOrThrow(turnId);
        return switch (targetState) {
            case STARTED -> startTurn(turn, targetState);
            case ENDED -> endTurn(turn, targetState);
            default -> {
                String errorMessage = "The given target state %s is not valid"
                        .formatted(targetState);
                throw new IllegalStateException(errorMessage);
            }
        };
    }

    private Turn startTurn(Turn turn, TurnStateValue targetState) {
        return transactionTemplate.execute(updateTurnCurrentState(turn.getId(), STARTED));
    }

    private Turn endTurn(Turn turn, TurnStateValue targetState) {
        throw new IllegalStateException("Not implemented yet");
    }
}
