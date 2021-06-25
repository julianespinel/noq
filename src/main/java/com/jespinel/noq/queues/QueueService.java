package com.jespinel.noq.queues;

import com.jespinel.noq.branches.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class QueueService {

    private final BranchService branchService;
    private final QueueRepository repository;

    @Autowired
    QueueService(BranchService branchService, QueueRepository repository) {
        this.branchService = branchService;
        this.repository = repository;
    }

    Queue create(Queue queue) {
        branchService.getOrThrow(queue.getBranchId());
        return repository.saveOrThrow(queue);
    }
}
