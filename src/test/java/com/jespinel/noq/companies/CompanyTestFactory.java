package com.jespinel.noq.companies;

import com.github.javafaker.Faker;

public class CompanyTestFactory {

    private static final Faker faker = new Faker();

    static Company getRandomCompany() {
        String nit = faker.idNumber().valid();
        String name = faker.name().name();
        return new Company(nit, name);
    }
}
