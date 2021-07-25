package com.jespinel.noq.turns;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public class Turn {

    private long id;
    private final String phoneNumber;
    private final long queueId;
    private final TurnNumber turnNumber;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Turn(String phoneNumber, long queueId) {
        this.phoneNumber = phoneNumber;
        this.queueId = queueId;
        this.turnNumber = null;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @JsonCreator
    public Turn(long id, String phoneNumber, long queueId, TurnNumber turnNumber,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.queueId = queueId;
        this.turnNumber = turnNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Turn(String phoneNumber, long queueId, TurnNumber turnNumber) {
        this.phoneNumber = phoneNumber;
        this.queueId = queueId;
        this.turnNumber = turnNumber;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Turn(long id, Turn turn) {
        this.id = id;
        this.phoneNumber = turn.getPhoneNumber();
        this.queueId = turn.getQueueId();
        this.turnNumber = turn.getTurnNumber();
        this.createdAt = turn.getCreatedAt();
        this.updatedAt = turn.getUpdatedAt();
    }

    public long getId() {
        return id;
    }

    public TurnNumber getTurnNumber() {
        return turnNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getQueueId() {
        return queueId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
