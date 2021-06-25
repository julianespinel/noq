package com.jespinel.noq.queues;

import com.jespinel.noq.common.exceptions.ValidationException;
import org.apache.logging.log4j.util.Strings;

public record CreateQueueRequest(String name, long branchId) {

    public void validateOrThrow() {
        if (Strings.isBlank(name)) {
            throw new ValidationException("The given name was null or empty");
        }
        if (branchId <= 0) {
            throw new ValidationException("The given branch id is not valid");
        }
    }

    public Queue toQueue() {
        return new Queue(name, branchId);
    }

    public static CreateQueueRequest from(Queue queue) {
        return new CreateQueueRequest(queue.getName(), queue.getBranchId());
    }
}
