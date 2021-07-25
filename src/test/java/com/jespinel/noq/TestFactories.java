package com.jespinel.noq;

import com.github.javafaker.Faker;
import com.jespinel.noq.branches.Branch;
import com.jespinel.noq.branches.CreateBranchRequest;
import com.jespinel.noq.companies.Company;
import com.jespinel.noq.queues.CreateQueueRequest;
import com.jespinel.noq.queues.Queue;
import com.jespinel.noq.turns.CreateTurnRequest;
import com.jespinel.noq.turns.TurnNumber;

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

    public static Queue getRandomQueue() {
        int branchId = faker.number().randomDigitNotZero();
        String name = faker.name().name();
        TurnNumber initialTurn = TurnNumber.from("A0");
        return new Queue(name, initialTurn, branchId);
    }

    public static Queue getRandomQueue(long branchId) {
        String name = faker.name().name();
        TurnNumber turnNumber = TurnNumber.from("A0");
        return new Queue(name, turnNumber, branchId);
    }

    public static Queue getRandomQueue(long branchId, String initialTurn) {
        String name = faker.name().name();
        TurnNumber turnNumber = TurnNumber.from(initialTurn);
        return new Queue(name, turnNumber, branchId);
    }

    public static CreateQueueRequest getCreateQueueRequest(long branchId) {
        Queue queue = getRandomQueue(branchId);
        String initialTurn = "A0";
        return new CreateQueueRequest(queue.getName(), initialTurn, queue.getBranchId());
    }

    public static CreateQueueRequest getCreateQueueRequest(Queue queue) {
        return CreateQueueRequest.from(queue);
    }

    public static CreateTurnRequest getCreateTurnRequest(String phoneNumber, long queueId) {
        return new CreateTurnRequest(phoneNumber, queueId);
    }
}
