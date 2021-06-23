package com.jespinel.noq.companies;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final CompanyRepository repository;

    @Autowired
    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    Company create(Company company) {
        try {
            return repository.save(company);
        } catch (DuplicateKeyException e) {
            String errorMessage = "A company with nit %s already exists".formatted(company.getNit());
            throw new DuplicatedEntityException(errorMessage);
        }
    }
}
