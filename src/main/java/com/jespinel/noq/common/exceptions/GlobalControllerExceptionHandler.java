package com.jespinel.noq.common.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleValidationErrors(ValidationException exception) {
        logger.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(DuplicatedEntityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleValidationErrors(DuplicatedEntityException exception) {
        logger.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleValidationErrors(EntityNotFoundException exception) {
        logger.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(EmptyQueueException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleValidationErrors(EmptyQueueException exception) {
        logger.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(TurnStateTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleValidationErrors(TurnStateTransitionException exception) {
        logger.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
}
