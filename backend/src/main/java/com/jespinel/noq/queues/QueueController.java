package com.jespinel.noq.queues;

import com.jespinel.noq.common.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/queues")
public class QueueController {

    final Logger logger = LoggerFactory.getLogger(QueueController.class);

    private final QueueService service;

    @Autowired
    QueueController(QueueService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Queue> create(@RequestBody CreateQueueRequest request) {
        request.validateOrThrow();
        Queue queue = service.create(request.toQueue());
        logger.debug("The queue %s was created for branch %s".formatted(request.name(), request.branchId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(queue);
    }

    /**
     * Queries the queues of a branch. Returns a paginated result.
     *
     * @param branchId The ID of the branch who owns the queues.
     * @param page Page to return, pagination starts from 0 (zero).
     * @return A page of queues from the given branch.
     */
    @GetMapping
    public ResponseEntity<Page<Queue>> getQueues(@RequestParam long branchId, @RequestParam int page) {
        validateOrThrowParams(branchId, page);
        Page<Queue> queues = service.getQueues(branchId, page);
        return ResponseEntity.ok(queues);
    }

    private void validateOrThrowParams(long branchId, int page) {
        if (branchId <= 0) {
            throw new ValidationException("Branch ID must be greater than 0");
        }
        if (page < 0) {
            throw new ValidationException("Page must be greater or equal to 0");
        }
    }
}
