package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateTurnRequestTest {

    @Test
    void validateOrThrow_shouldThrow_GivenAnEmptyPhoneNumber() {
        CreateTurnRequest createTurnRequest = new CreateTurnRequest("", 1);
        assertThrows(ValidationException.class, createTurnRequest::validateOrThrow);
    }

    @Test
    void validateOrThrow_shouldThrow_GivenAPhoneNumber_NoCountryCode() {
        CreateTurnRequest createTurnRequest = new CreateTurnRequest("3002930008", 1);
        assertThrows(ValidationException.class, createTurnRequest::validateOrThrow);
    }

    @Test
    void validateOrThrow_shouldThrow_GivenAPhoneNumber_WithDashes() {
        CreateTurnRequest createTurnRequest = new CreateTurnRequest("300-293-0008", 1);
        assertThrows(ValidationException.class, createTurnRequest::validateOrThrow);
    }

    @Test
    void validateOrThrow_shouldThrow_GivenALandLinePhoneNumber() {
        CreateTurnRequest createTurnRequest = new CreateTurnRequest("031-465-4474", 1);
        assertThrows(ValidationException.class, createTurnRequest::validateOrThrow);
    }

    @Test
    void validateOrThrow_shouldThrow_GivenAPhoneNumberFromPeru() {
        CreateTurnRequest createTurnRequest = new CreateTurnRequest("+51912345678", 1);
        assertThrows(ValidationException.class, createTurnRequest::validateOrThrow);
    }

    @Test
    void validateOrThrow_shouldValidate_GivenAPhoneNumber_WithCountryCode() {
        CreateTurnRequest createTurnRequest = new CreateTurnRequest("+573002930008", 1);
        createTurnRequest.validateOrThrow();
    }
}
