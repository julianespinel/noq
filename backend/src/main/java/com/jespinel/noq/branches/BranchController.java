package com.jespinel.noq.branches;

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
@RequestMapping("/api/branches")
class BranchController {

    final Logger logger = LoggerFactory.getLogger(BranchController.class);

    private final BranchService branchService;

    @Autowired
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    public ResponseEntity<Branch> create(@RequestBody CreateBranchRequest request) {
        request.validateOrThrow();
        Branch branch = branchService.create(request.toBranch());
        logger.debug("The branch %s from company %s was created".formatted(request.name(), request.companyId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(branch);
    }
}
