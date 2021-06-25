package com.jespinel.noq.branches;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class BranchRowMapper implements RowMapper<Branch> {

    @Override
    public Branch mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        long companyId = rs.getLong("companyId");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return new Branch(id, name, companyId, createdAt, updatedAt);
    }
}
