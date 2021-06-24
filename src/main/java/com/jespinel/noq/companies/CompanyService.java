package com.jespinel.noq.companies;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import com.jespinel.noq.common.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository repository;

    @Autowired
    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public Company create(Company company) {
        try {
            return repository.save(company);
        } catch (DuplicateKeyException e) {
            String errorMessage = "A company with nit %s already exists".formatted(company.getNit());
            throw new DuplicatedEntityException(errorMessage);
        }
    }

    public Company getOrThrow(long companyId) {
        Optional<Company> company = repository.find(companyId);
        company.orElseThrow(() -> {
            String errorMessage = "The company with ID %s was not found".formatted(companyId);
            throw new EntityNotFoundException(errorMessage);
        });
        return company.get();
    }
}
