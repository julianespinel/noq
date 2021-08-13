package com.jespinel.noq.companies;

import org.springframework.beans.factory.annotation.Autowired;
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
class CompanyRepository {

    private static final String CREATE_COMPANY_SQL =
            "INSERT INTO companies (tin, name, created_at, updated_at) " +
                    "VALUES (:tin, :name, :created_at, :updated_at)";

    private static final String FIND_COMPANY_SQL =
            "SELECT * FROM companies WHERE id = :id";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    CompanyRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Company save(Company company) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("tin", company.getTin())
                .addValue("name", company.getName())
                .addValue("created_at", company.getCreatedAt())
                .addValue("updated_at", company.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_COMPANY_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Company(id, company);
    }

    Optional<Company> find(long companyId) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", companyId);
        List<Company> companies = jdbcTemplate.query(FIND_COMPANY_SQL, params, new CompanyRowMapper());
        Company company = DataAccessUtils.singleResult(companies);
        return Optional.ofNullable(company);
    }
}
