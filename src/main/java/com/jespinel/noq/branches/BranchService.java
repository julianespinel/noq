package com.jespinel.noq.branches;

import com.jespinel.noq.common.exceptions.EntityNotFoundException;
import com.jespinel.noq.companies.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BranchService {

    private final BranchRepository repository;

    private final CompanyService companyService;

    @Autowired
    public BranchService(CompanyService companyService, BranchRepository repository) {
        this.companyService = companyService;
        this.repository = repository;
    }

    public Branch create(Branch branch) {
        companyService.getOrThrow(branch.getCompanyId());
        return repository.saveOrThrow(branch);
    }

    public Branch getOrThrow(long branchId) {
        Optional<Branch> branch = repository.find(branchId);
        branch.orElseThrow(() -> {
            String errorMessage = "The branch with ID %s was not found".formatted(branchId);
            throw new EntityNotFoundException(errorMessage);
        });
        return branch.get();
    }
}
