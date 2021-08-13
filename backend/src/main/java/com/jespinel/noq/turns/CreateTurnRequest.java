package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;
import com.jespinel.noq.common.validators.PhoneNumbers;

public record CreateTurnRequest(String phoneNumber, long queueId) {

    public void validateOrThrow() {
        if (!PhoneNumbers.isValid(phoneNumber)) {
            throw new ValidationException("The given phone number is not valid");
        }
        if (queueId <= 0) {
            throw new ValidationException("The given queue id is not valid");
        }
    }

    public static CreateTurnRequest from(Turn turn) {
        return new CreateTurnRequest(turn.getPhoneNumber(), turn.getQueueId());
    }
}
