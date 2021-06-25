package com.jespinel.noq;

import com.github.javafaker.Faker;
import com.jespinel.noq.branches.Branch;
import com.jespinel.noq.branches.CreateBranchRequest;
import com.jespinel.noq.companies.Company;

public class TestFactories {

    private static final Faker faker = new Faker();

    public static Company getRandomCompany() {
        String nit = faker.idNumber().valid();
        String name = faker.name().name();
        return new Company(nit, name);
    }

    public static Branch getRandomBranch(long parentId) {
        String name = faker.name().name();
        return new Branch(name, parentId);
    }

    public static CreateBranchRequest getCreateBranchRequest(long parentId) {
        Branch branch = getRandomBranch(parentId);
        return new CreateBranchRequest(branch.getName(), branch.getCompanyId());
    }

    public static CreateBranchRequest getCreateBranchRequest(Branch branch) {
        return CreateBranchRequest.from(branch);
    }
}
