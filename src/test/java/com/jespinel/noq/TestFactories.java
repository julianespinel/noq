package com.jespinel.noq;

import com.github.javafaker.Faker;
import com.jespinel.noq.branches.Branch;
import com.jespinel.noq.branches.BranchService;
import com.jespinel.noq.branches.CreateBranchRequest;
import com.jespinel.noq.companies.Company;
import com.jespinel.noq.companies.CompanyService;
import com.jespinel.noq.queues.CreateQueueRequest;
import com.jespinel.noq.queues.Queue;
import com.jespinel.noq.queues.QueueService;
import com.jespinel.noq.turns.CreateTurnRequest;
import com.jespinel.noq.turns.TurnNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestFactories {

    private static final Faker faker = new Faker();

    private final CompanyService companyService;
    private final BranchService branchService;
    private final QueueService queueService;

    @Autowired
    public TestFactories(CompanyService companyService, BranchService branchService, QueueService queueService) {
        this.companyService = companyService;
        this.branchService = branchService;
        this.queueService = queueService;
    }

    public Company getRandomCompany() {
        String tin = faker.idNumber().valid();
        String name = faker.name().name();
        return new Company(tin, name);
    }

    public Branch getRandomBranch(long companyId) {
        String name = faker.name().name();
        return new Branch(name, companyId);
    }

    public CreateBranchRequest getCreateBranchRequest(long companyId) {
        Branch branch = getRandomBranch(companyId);
        return new CreateBranchRequest(branch.getName(), branch.getCompanyId());
    }

    public CreateBranchRequest getCreateBranchRequest(Branch branch) {
        return CreateBranchRequest.from(branch);
    }

    public Queue getRandomQueue() {
        int branchId = faker.number().randomDigitNotZero();
        String name = faker.name().name();
        TurnNumber initialTurn = TurnNumber.from("A0");
        return new Queue(name, initialTurn, branchId);
    }

    public Queue getRandomQueue(long branchId) {
        String name = faker.name().name();
        TurnNumber turnNumber = TurnNumber.from("A0");
        return new Queue(name, turnNumber, branchId);
    }

    public Queue getRandomQueue(long branchId, String initialTurn) {
        String name = faker.name().name();
        TurnNumber turnNumber = TurnNumber.from(initialTurn);
        return new Queue(name, turnNumber, branchId);
    }

    public CreateQueueRequest getCreateQueueRequest(long branchId) {
        Queue queue = getRandomQueue(branchId);
        String initialTurn = "A0";
        return new CreateQueueRequest(queue.getName(), initialTurn, queue.getBranchId());
    }

    public CreateQueueRequest getCreateQueueRequest(Queue queue) {
        return CreateQueueRequest.from(queue);
    }

    public CreateTurnRequest getCreateTurnRequest(String phoneNumber, long queueId) {
        return new CreateTurnRequest(phoneNumber, queueId);
    }

    public Queue createTestQueueInDB() {
        Company company = getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = getRandomBranch(createdCompany.getId());
        Branch createdBranch = branchService.create(branch);

        Queue queue = getRandomQueue(createdBranch.getId());
        return queueService.create(queue);
    }

    public Queue createTestQueueInDB(String initialTurn) {
        Company company = getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = getRandomBranch(createdCompany.getId());
        Branch createdBranch = branchService.create(branch);

        Queue queue = getRandomQueue(createdBranch.getId(), initialTurn);
        return queueService.create(queue);
    }
}
