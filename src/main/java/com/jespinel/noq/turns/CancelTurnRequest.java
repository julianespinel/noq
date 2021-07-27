package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;
import com.jespinel.noq.common.validators.PhoneNumbers;

public record CancelTurnRequest(String phoneNumber) {

    public void validateOrThrow() {
        if (!PhoneNumbers.isValid(phoneNumber)) {
            throw new ValidationException("The given phone number is not valid");
        }
    }
}
