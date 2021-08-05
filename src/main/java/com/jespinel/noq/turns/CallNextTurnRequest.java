package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;

public record CallNextTurnRequest(long queueId) {

    public void validateOrThrow() {
        if (queueId <= 0) {
            throw new ValidationException("The given queue id is not valid");
        }
    }
}
