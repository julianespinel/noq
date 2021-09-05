package com.jespinel.noq.branches;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public class Branch {

    private long id;
    private final String name;
    private final long companyId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Branch(long id, Branch branch) {
        this.id = id;
        name = branch.name;
        companyId = branch.companyId;
        createdAt = branch.createdAt;
        updatedAt = branch.updatedAt;
    }

    public Branch(String name, long companyId) {
        this.name = name;
        this.companyId = companyId;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @JsonCreator
    public Branch(long id, String name, long companyId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCompanyId() {
        return companyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
