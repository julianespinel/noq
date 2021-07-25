package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CreateTurnRequest(String phoneNumber, long queueId) {

    public void validateOrThrow() {
        if (!isValid(phoneNumber)) {
            throw new ValidationException("The given phone number is not valid");
        }
        if (queueId <= 0) {
            throw new ValidationException("The given queue id is not valid");
        }
    }

    /**
     * Method to validate phone numbers
     * (we need to improve this validation)
     *
     * @param phoneNumber The phone number we want to validate
     * @return true if phone number is valid, otherwise returns false
     */
    private boolean isValid(String phoneNumber) {
        Pattern colombianCellphonePatter = Pattern.compile("^\\+573[0-9]{9}$");
        Matcher matcher = colombianCellphonePatter.matcher(phoneNumber);
        return matcher.matches();
    }

    public static CreateTurnRequest from(Turn turn) {
        return new CreateTurnRequest(turn.getPhoneNumber(), turn.getQueueId());
    }
}
