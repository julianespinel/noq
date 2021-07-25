package com.jespinel.noq.turns;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TurnStateRowMapper implements RowMapper<TurnState> {

    @Override
    public TurnState mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        int turnId = rs.getInt("turn_id");

        String state = rs.getString("state");
        TurnStateValue turnStateValue = TurnStateValue.valueOf(state);

        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return new TurnState(id, turnId, turnStateValue, createdAt, updatedAt);
    }
}
