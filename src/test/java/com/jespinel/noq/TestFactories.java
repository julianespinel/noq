package com.jespinel.noq;

import com.github.javafaker.Faker;
import com.jespinel.noq.branches.Branch;
import com.jespinel.noq.branches.CreateBranchRequest;
import com.jespinel.noq.companies.Company;
import com.jespinel.noq.queues.CreateQueueRequest;
import com.jespinel.noq.queues.Queue;

public class TestFactories {

    private static final Faker faker = new Faker();

    public static Company getRandomCompany() {
        String nit = faker.idNumber().valid();
        String name = faker.name().name();
        return new Company(nit, name);
    }

    public static Branch getRandomBranch(long companyId) {
        String name = faker.name().name();
        return new Branch(name, companyId);
    }

    public static CreateBranchRequest getCreateBranchRequest(long companyId) {
        Branch branch = getRandomBranch(companyId);
        return new CreateBranchRequest(branch.getName(), branch.getCompanyId());
    }

    public static CreateBranchRequest getCreateBranchRequest(Branch branch) {
        return CreateBranchRequest.from(branch);
    }

    public static Queue getRandomQueue(long branchId) {
        String name = faker.name().name();
        return new Queue(name, branchId);
    }

    public static CreateQueueRequest getCreateQueueRequest(long branchId) {
        Queue queue = getRandomQueue(branchId);
        return new CreateQueueRequest(queue.getName(), queue.getBranchId());
    }

    public static CreateQueueRequest getCreateQueueRequest(Queue queue) {
        return CreateQueueRequest.from(queue);
    }
}
