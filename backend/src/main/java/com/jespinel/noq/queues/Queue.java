package com.jespinel.noq.queues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jespinel.noq.turns.TurnNumber;

import java.time.LocalDateTime;

public class Queue {

    private long id;
    private final String name;
    private final TurnNumber initialTurn;
    private final long branchId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Queue(long id, Queue queue) {
        this.id = id;
        name = queue.name;
        initialTurn = queue.getInitialTurn();
        branchId = queue.branchId;
        createdAt = queue.createdAt;
        updatedAt = queue.updatedAt;
    }

    public Queue(String name, TurnNumber initialTurn, long branchId) {
        this.name = name;
        this.initialTurn = initialTurn;
        this.branchId = branchId;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @JsonCreator
    public Queue(long id, String name, TurnNumber initialTurn, long branchId,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.initialTurn = initialTurn;
        this.branchId = branchId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getBranchId() {
        return branchId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public TurnNumber getInitialTurn() {
        return initialTurn;
    }
}
