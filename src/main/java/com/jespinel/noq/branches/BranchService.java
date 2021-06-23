package com.jespinel.noq.branches;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class BranchService {

    private final BranchRepository repository;

    @Autowired
    public BranchService(BranchRepository repository) {
        this.repository = repository;
    }

    public Branch create(Branch branch) {
        try {
            return repository.save(branch);
        } catch (DuplicateKeyException e) {
            String errorMessage = "A branch with name %s and parent %s already exists"
                    .formatted(branch.getName(), branch.getParentId());
            throw new DuplicatedEntityException(errorMessage);
        }
    }
}
