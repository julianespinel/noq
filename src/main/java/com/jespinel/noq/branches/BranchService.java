package com.jespinel.noq.branches;

import com.jespinel.noq.companies.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        companyService.getOrThrow(branch.getParentId());
        return repository.saveOrThrow(branch);
    }
}
