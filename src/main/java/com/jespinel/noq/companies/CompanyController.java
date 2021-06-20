package com.jespinel.noq.companies;

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
@RequestMapping("/api/companies")
public class CompanyController {

    Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService service;

    @Autowired
    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Company> create(@RequestBody CreateCompanyRequest request) {
        request.validateOrThrow();
        Company company = service.create(request.toCompany());
        logger.debug("The company %s %s was created".formatted(request.nit(), request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }
}
