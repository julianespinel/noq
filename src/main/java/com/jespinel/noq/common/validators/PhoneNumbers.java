package com.jespinel.noq.common.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumbers {

    /**
     * Method to validate phone numbers
     * (we need to improve this validation)
     *
     * @param phoneNumber The phone number we want to validate
     * @return true if phone number is valid, otherwise returns false
     */
    public static boolean isValid(String phoneNumber) {
        Pattern colombianCellphonePatter = Pattern.compile("^\\+573[0-9]{9}$");
        Matcher matcher = colombianCellphonePatter.matcher(phoneNumber);
        return matcher.matches();
    }
}
