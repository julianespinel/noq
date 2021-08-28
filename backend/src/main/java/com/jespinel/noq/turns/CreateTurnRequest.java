package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;
import com.jespinel.noq.common.validators.PhoneNumbers;

public record CreateTurnRequest(String phoneNumber, long queueId) {

    public void validateOrThrow() {
        if (!PhoneNumbers.isValid(phoneNumber)) {
            String message = "The given phone number %s is not valid".formatted(phoneNumber);
            throw new ValidationException(message);
        }
        if (queueId <= 0) {
            String message = "The given queue id %s is not valid".formatted(queueId);
            throw new ValidationException(message);
        }
    }

    public static CreateTurnRequest from(Turn turn) {
        return new CreateTurnRequest(turn.getPhoneNumber(), turn.getQueueId());
    }
}
