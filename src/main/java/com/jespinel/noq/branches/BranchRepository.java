package com.jespinel.noq.branches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
class BranchRepository {

    private static final String CREATE_BRANCH_SQL =
            "INSERT INTO branches (name, parentNit, created_at, updated_at) " +
                    "VALUES (:name, :parentNit, :created_at, :updated_at)";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    BranchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Branch save(Branch branch) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", branch.getName())
                .addValue("parentNit", branch.getParentId())
                .addValue("created_at", branch.getCreatedAt())
                .addValue("updated_at", branch.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_BRANCH_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Branch(id, branch);
    }
}
