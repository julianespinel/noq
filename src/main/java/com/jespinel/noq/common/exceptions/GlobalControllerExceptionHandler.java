package com.jespinel.noq.common.exceptions;

import com.jespinel.noq.companies.DuplicatedCompanyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationErrors(ValidationException exception) {
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(DuplicatedCompanyException.class)
    public ResponseEntity<ApiError> handleValidationErrors(DuplicatedCompanyException exception) {
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
}
