package com.jespinel.noq.companies;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public class Company {

    private long id;
    private final String nit;
    private final String name;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Company(String nit, String name) {
        this.nit = nit;
        this.name = name;

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    public Company(long id, Company company) {
        this.id = id;
        nit = company.nit;
        name = company.name;
        createdAt = company.createdAt;
        updatedAt = company.updatedAt;
    }

    @JsonCreator
    public Company(long id, String nit, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nit = nit;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getNit() {
        return nit;
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
