package com.jespinel.noq.companies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
class CompanyService {

    private final CompanyRepository repository;

    @Autowired
    CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    Company create(Company company) throws DuplicatedCompanyException {
        try {
            return repository.save(company);
        } catch (DuplicateKeyException e) {
            String errorMessage = "A company with nit %s already exists".formatted(company.getNit());
            throw new DuplicatedCompanyException(errorMessage);
        }
    }
}
