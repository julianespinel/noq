package com.jespinel.noq.companies;

import com.jespinel.noq.common.exceptions.ValidationException;
import org.apache.logging.log4j.util.Strings;

public record CreateCompanyRequest(String nit, String name) {

    void validateOrThrow() throws ValidationException {
        if (Strings.isBlank(nit)) {
            throw new ValidationException("The given nit was null or empty");
        }
        if (Strings.isBlank(name)) {
            throw new ValidationException("The given name was null or empty");
        }
    }

    Company toCompany() {
        return new Company(nit, name);
    }
}
