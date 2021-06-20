package com.jespinel.noq.companies;

public class DuplicatedCompanyException extends RuntimeException {

    public DuplicatedCompanyException(String errorMessage) {
        super(errorMessage);
    }
}
