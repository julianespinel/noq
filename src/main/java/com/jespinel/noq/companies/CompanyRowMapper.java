package com.jespinel.noq.companies;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CompanyRowMapper implements RowMapper<Company> {

    @Override
    public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String tin = rs.getString("tin");
        String name = rs.getString("name");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return new Company(id, tin, name, createdAt, updatedAt);
    }
}
