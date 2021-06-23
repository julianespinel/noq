package com.jespinel.noq.branches;

import com.jespinel.noq.common.exceptions.ValidationException;
import org.apache.logging.log4j.util.Strings;

public record CreateBranchRequest(String name, long companyId) {

    public void validateOrThrow() {
        if (Strings.isBlank(name)) {
            throw new ValidationException("The given name was null or empty");
        }
        if (companyId <= 0) {
            throw new ValidationException("The given company id is not valid");
        }
    }

    public Branch toBranch() {
        return new Branch(name, companyId);
    }
}
