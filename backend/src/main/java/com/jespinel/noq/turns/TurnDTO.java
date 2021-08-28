package com.jespinel.noq.turns;

import java.time.LocalDateTime;

public record TurnDTO(
        long id,
        String phoneNumber,
        long queueId,
        String turnNumber,
        TurnStateValue currentState,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TurnDTO from(Turn turn) {
        return new TurnDTO(
                turn.getId(),
                turn.getPhoneNumber(),
                turn.getQueueId(),
                turn.getTurnNumber().toString(),
                turn.getCurrentState(),
                turn.getCreatedAt(),
                turn.getUpdatedAt()
        );
    }
}
