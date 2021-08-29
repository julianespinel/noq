package com.jespinel.noq.queues;

import com.jespinel.noq.branches.BranchService;
import com.jespinel.noq.common.exceptions.EntityNotFoundException;
import com.jespinel.noq.turns.TurnNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class QueueService {

    private final QueueRepository repository;
    private final BranchService branchService;
    private final TurnNumberService turnNumberService;

    @Autowired
    QueueService(QueueRepository repository, BranchService branchService,
                 TurnNumberService turnNumberService) {
        this.repository = repository;
        this.branchService = branchService;
        this.turnNumberService = turnNumberService;
    }

    public Queue create(Queue queue) {
        branchService.getOrThrow(queue.getBranchId());
        Queue createdQueue = repository.saveOrThrow(queue);
        turnNumberService.setInitialTurn(createdQueue);
        return createdQueue;
    }

    public Queue getOrThrow(long queueId) {
        Optional<Queue> queue = repository.find(queueId);
        queue.orElseThrow(() -> {
            String errorMessage = "The queue with ID %s was not found".formatted(queueId);
            throw new EntityNotFoundException(errorMessage);
        });
        return queue.get();
    }

    public Page<Queue> getQueues(long branchId, int page) {
        return repository.findByBranchId(branchId, page);
    }
}
