package com.jespinel.noq.branches;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public class Branch {

    private long id;
    private final String name;
    private final long parentId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Branch(long id, Branch branch) {
        this.id = id;
        name = branch.name;
        parentId = branch.parentId;
        createdAt = branch.createdAt;
        updatedAt = branch.updatedAt;
    }

    public Branch(String name, long parentId) {
        this.name = name;
        this.parentId = parentId;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @JsonCreator
    public Branch(long id, String name, long parentId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getParentId() {
        return parentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
