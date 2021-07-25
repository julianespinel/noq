package com.jespinel.noq.turns;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TurnRowMapper implements RowMapper<Turn> {

    @Override
    public Turn mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String phoneNumber = rs.getString("phone_number");
        long queueId = rs.getLong("queue_id");

        String turnNumberString = rs.getString("turn_number");
        TurnNumber turnNumber = TurnNumber.from(turnNumberString);

        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return new Turn(id, phoneNumber, queueId, turnNumber, createdAt, updatedAt);
    }
}
