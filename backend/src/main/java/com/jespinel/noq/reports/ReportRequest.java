package com.jespinel.noq.reports;

import com.jespinel.noq.common.exceptions.ValidationException;

import java.time.LocalDateTime;

public record ReportRequest(long queueId, LocalDateTime initialDate, LocalDateTime finalDate) {

    public void validateOrThrow() {
        if (queueId <= 0) {
            throw new ValidationException("The given queue id is not valid");
        }

        if (initialDate.isAfter(finalDate)) {
            throw new ValidationException("The given initial date should be before the final date");
        }
    }
}
