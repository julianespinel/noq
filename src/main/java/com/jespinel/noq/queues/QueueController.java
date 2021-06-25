package com.jespinel.noq.queues;

import com.jespinel.noq.companies.Company;
import com.jespinel.noq.companies.CompanyController;
import com.jespinel.noq.companies.CompanyService;
import com.jespinel.noq.companies.CreateCompanyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/queues")
public class QueueController {

    Logger logger = LoggerFactory.getLogger(QueueController.class);

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
}
