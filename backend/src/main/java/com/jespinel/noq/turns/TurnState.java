package com.jespinel.noq.turns;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public class TurnState {

    private long id;
    private final long turnId;
    private final TurnStateValue state;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TurnState(long turnId, TurnStateValue state) {
        this.turnId = turnId;
        this.state = state;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @JsonCreator
    public TurnState(long id, long turnId, TurnStateValue state, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.turnId = turnId;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TurnState from(long id, TurnState turnState) {
        return new TurnState(id, turnState.getTurnId(), turnState.getState(),
                turnState.getCreatedAt(), turnState.getUpdatedAt());
    }

    public long getId() {
        return id;
    }

    public long getTurnId() {
        return turnId;
    }

    public TurnStateValue getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
