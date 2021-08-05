package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.EntityNotFoundException;
import com.jespinel.noq.common.exceptions.TurnStateTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TurnStateService {

    final Logger logger = LoggerFactory.getLogger(TurnStateService.class);

    /**
     * Define state machine for turns
     */
    private static final Map<TurnStateValue, List<TurnStateValue>> stateToTransitions;

    static {
        stateToTransitions = new HashMap<>();

        stateToTransitions.put(TurnStateValue.REQUESTED, Arrays.asList(
                TurnStateValue.READY, TurnStateValue.CANCELLED)
        );

        stateToTransitions.put(TurnStateValue.READY, Arrays.asList(
                TurnStateValue.CANCELLED, TurnStateValue.STARTED)
        );

        stateToTransitions.put(TurnStateValue.STARTED, Collections.singletonList(
                TurnStateValue.ENDED)
        );
    }

    private final TurnStateRepository turnStateRepository;

    @Autowired
    TurnStateService(TurnStateRepository turnStateRepository) {
        this.turnStateRepository = turnStateRepository;
    }

    TurnState create(long turnId) {
        TurnState requested = new TurnState(turnId, TurnStateValue.REQUESTED);
        return turnStateRepository.save(requested);
    }

    /**
     * Creates a new TurnState with the given targetState as the state.
     * This method inserts a new row in the table turn_states.
     *
     * @param turnId      The ID of the turn we want to update to a new state.
     * @param targetState The new state of the turn.
     * @return The new TurnState saved in the database.
     */
    TurnState moveToState(long turnId, TurnStateValue targetState) {
        TurnState currentState = getOrThrowByTurnId(turnId);
        TurnState nextState = moveToStateInternal(currentState, targetState);
        return turnStateRepository.save(nextState);
    }

    private TurnState moveToStateInternal(TurnState currentState, TurnStateValue targetStateValue) {
        long turnId = currentState.getTurnId();
        TurnStateValue currentStateValue = currentState.getState();

        List<TurnStateValue> transitionValues = stateToTransitions.get(currentStateValue);
        if (transitionValues.contains(targetStateValue)) {
            logStateTransition(targetStateValue, turnId, currentStateValue);
            return new TurnState(turnId, targetStateValue);
        }

        String template = "Turn %s can't transition from %s to %s";
        String errorMessage = template.formatted(turnId, currentStateValue, targetStateValue);
        throw new TurnStateTransitionException(errorMessage);
    }

    private void logStateTransition(TurnStateValue targetStateValue, long turnId, TurnStateValue currentStateValue) {
        String template = "Turn %s transitioning from %s to %s";
        String message = template.formatted(turnId, currentStateValue, targetStateValue);
        logger.info(message);
    }

    private TurnState getOrThrowByTurnId(long turnId) {
        Optional<TurnState> currentState = turnStateRepository.findByTurnId(turnId);
        currentState.orElseThrow(() -> {
            String errorMessage = "Turn %s does not have states".formatted(turnId);
            throw new EntityNotFoundException(errorMessage);
        });
        return currentState.get();
    }
}
