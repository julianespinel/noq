package com.jespinel.noq.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jespinel.noq.turns.TurnStateValue;

public record TimePerState(TurnStateValue state, long milliseconds) {

    @JsonCreator
    public TimePerState {
    }
}
