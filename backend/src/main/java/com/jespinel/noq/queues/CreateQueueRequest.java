package com.jespinel.noq.queues;

import com.jespinel.noq.common.exceptions.ValidationException;
import com.jespinel.noq.turns.TurnNumber;
import org.apache.logging.log4j.util.Strings;

public record CreateQueueRequest(String name, String initialTurn, long branchId) {

    public void validateOrThrow() {
        if (Strings.isBlank(name)) {
            throw new ValidationException("The given name was null or empty");
        }

        boolean isValidTurnNumber = TurnNumber.isValid(initialTurn);
        if (!isValidTurnNumber) {
            String template = "The given initial turn %s is not valid, " +
                    "it should be a single letter followed by a digit";
            String errorMessage = template.formatted(initialTurn);
            throw new ValidationException(errorMessage);
        }

        if (branchId <= 0) {
            throw new ValidationException("The given branch id is not valid");
        }
    }

    public Queue toQueue() {
        TurnNumber turnNumber = TurnNumber.from(initialTurn);
        return new Queue(name, turnNumber, branchId);
    }

    public static CreateQueueRequest from(Queue queue) {
        return new CreateQueueRequest(queue.getName(), queue.getInitialTurn().toString(), queue.getBranchId());
    }
}
