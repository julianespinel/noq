package com.jespinel.noq.companies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
class CompanyRepository {

    private static final String CREATE_COMPANY_SQL =
            "INSERT INTO companies (nit, name, created_at, updated_at) " +
                    "VALUES (:nit, :name, :created_at, :updated_at)";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    CompanyRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Company save(Company company) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("nit", company.getNit())
                .addValue("name", company.getName())
                .addValue("created_at", company.getCreatedAt())
                .addValue("updated_at", company.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_COMPANY_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Company(id, company);
    }
}
