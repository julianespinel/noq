package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;

public record UpdateTurnRequest(String targetState) {

    private static final String STARTED = "started";
    private static final String ENDED = "ended";

    public void validateOrThrow() {
        String lowerTargetState = targetState.toLowerCase();
        boolean isValidTargetState = lowerTargetState.equals(STARTED) || lowerTargetState.equals(ENDED);
        if (!isValidTargetState) {
            throw new ValidationException("The given target state is not valid");
        }
    }
}
