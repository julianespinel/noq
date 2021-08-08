package com.jespinel.noq.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jespinel.noq.turns.TurnStateValue;

import java.util.Map;

public record TurnTimePerState(long averageCustomerWaitingTimeInSeconds,
                               long averageCompanyWaitingTimeInSeconds,
                               long averageTurnCancellationTimeInSeconds,
                               long averageTurnExecutionTimeInSeconds) {

    private static final long NON_EXISTENT = -1;

    @JsonCreator
    public TurnTimePerState {
    }

    public static TurnTimePerState from(Map<TurnStateValue, Long> statesToTime) {
        long averageCustomerWaitingTimeInSeconds = statesToTime.getOrDefault(TurnStateValue.READY, NON_EXISTENT);
        long averageCompanyWaitingTimeInSeconds = statesToTime.getOrDefault(TurnStateValue.STARTED, NON_EXISTENT);
        long averageTurnCancellationTimeInSeconds = statesToTime.getOrDefault(TurnStateValue.CANCELLED, NON_EXISTENT);
        long averageTurnExecutionTimeInSeconds = statesToTime.getOrDefault(TurnStateValue.ENDED, NON_EXISTENT);

        return new TurnTimePerState(
                averageCustomerWaitingTimeInSeconds,
                averageCompanyWaitingTimeInSeconds,
                averageTurnCancellationTimeInSeconds,
                averageTurnExecutionTimeInSeconds
        );
    }
}
