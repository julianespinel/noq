package com.jespinel.noq.branches;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
class BranchRepository {

    private static final String CREATE_BRANCH_SQL =
            "INSERT INTO branches (name, companyId, created_at, updated_at) " +
                    "VALUES (:name, :companyId, :created_at, :updated_at)";

    private static final String FIND_BRANCH_SQL =
            "SELECT * FROM branches WHERE id = :id";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    BranchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Branch save(Branch branch) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", branch.getName())
                .addValue("companyId", branch.getCompanyId())
                .addValue("created_at", branch.getCreatedAt())
                .addValue("updated_at", branch.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_BRANCH_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Branch(id, branch);
    }

    Branch saveOrThrow(Branch branch) {
        try {
            return save(branch);
        } catch (DuplicateKeyException e) {
            String errorMessage = "A branch with name %s from company %s already exists"
                    .formatted(branch.getName(), branch.getCompanyId());
            throw new DuplicatedEntityException(errorMessage);
        }
    }

    Optional<Branch> find(long branchId) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", branchId);
        List<Branch> branches = jdbcTemplate.query(FIND_BRANCH_SQL, params, new BranchRowMapper());
        Branch branch = DataAccessUtils.singleResult(branches);
        return Optional.ofNullable(branch);
    }
}
