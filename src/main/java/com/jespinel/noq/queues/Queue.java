package com.jespinel.noq.queues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jespinel.noq.branches.Branch;

import java.time.LocalDateTime;

public class Queue {

    private long id;
    private final String name;
    private final long branchId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Queue(long id, Queue queue) {
        this.id = id;
        name = queue.name;
        branchId = queue.branchId;
        createdAt = queue.createdAt;
        updatedAt = queue.updatedAt;
    }

    public Queue(String name, long branchId) {
        this.name = name;
        this.branchId = branchId;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @JsonCreator
    public Queue(long id, String name, long branchId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
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

}
