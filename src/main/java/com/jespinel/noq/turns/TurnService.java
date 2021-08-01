package com.jespinel.noq.turns;


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
        return new Turn(phoneNumber, queue.getId(), nextTurnNumber, TurnStateValue.REQUESTED);
    }

    public Optional<Turn> getPhoneNumberLatestTurn(String phoneNumber) {
        return repository.getLatestTurnByPhoneNumber(phoneNumber);
    }

    /**
     * Cancels the latest turn of the given phone number if it is in one of
     * the following states: Requested, Ready.
     *
     * @param phoneNumber The phone number associated to the turn we want to cancel
     * @return A Turn within an optional if the turn exists, otherwise an empty optional
     */
    public Optional<Turn> cancel(String phoneNumber) {
        Optional<Turn> latestTurn = getPhoneNumberLatestTurn(phoneNumber);
        if (latestTurn.isEmpty()) {
            return latestTurn;
        }

        try {
            Turn turn = latestTurn.get();
            turnStateService.moveToState(turn.getId(), TurnStateValue.CANCELLED);
            notificationService.notifyCancellation(turn);
            return latestTurn;
        } catch (IllegalStateException e) {
            logger.error(e.getMessage(), e);
            String errorMessage = "The turn has started, it cannot be cancelled";
            notificationService.notifyError(errorMessage, phoneNumber);
            return Optional.empty();
        }
    }
}
