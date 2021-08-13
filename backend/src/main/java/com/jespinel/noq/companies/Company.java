package com.jespinel.noq.companies;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public class Company {

    private long id;

    /**
     * Taxpayer Identification Number
     */
    private final String tin;

    private final String name;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Company(String tin, String name) {
        this.tin = tin;
        this.name = name;

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    public Company(long id, Company company) {
        this.id = id;
        tin = company.tin;
        name = company.name;
        createdAt = company.createdAt;
        updatedAt = company.updatedAt;
    }

    @JsonCreator
    public Company(long id, String tin, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tin = tin;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getTin() {
        return tin;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
