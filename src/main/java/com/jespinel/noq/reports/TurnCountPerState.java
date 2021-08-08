package com.jespinel.noq.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jespinel.noq.turns.TurnStateValue;

import java.util.Map;

public record TurnCountPerState(long requestedTurns, long readyTurns,
                                long cancelledTurns, long startedTurns,
                                long endedTurns) {

    private static final long NON_EXISTENT = -1;

    @JsonCreator
    public TurnCountPerState {
    }

    public static TurnCountPerState from(Map<TurnStateValue, Long> statesToCount) {
        long requested = statesToCount.getOrDefault(TurnStateValue.REQUESTED, NON_EXISTENT);
        long ready = statesToCount.getOrDefault(TurnStateValue.READY, NON_EXISTENT);
        long cancelled = statesToCount.getOrDefault(TurnStateValue.CANCELLED, NON_EXISTENT);
        long started = statesToCount.getOrDefault(TurnStateValue.STARTED, NON_EXISTENT);
        long ended = statesToCount.getOrDefault(TurnStateValue.ENDED, NON_EXISTENT);

        return new TurnCountPerState(requested, ready, cancelled, started, ended);
    }
}
