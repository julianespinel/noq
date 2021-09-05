package com.jespinel.noq.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jespinel.noq.turns.TurnStateValue;

public record CountPerState(TurnStateValue state, long count) {

    @JsonCreator
    public CountPerState {
    }
}
