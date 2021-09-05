package com.jespinel.noq.queues;

import com.jespinel.noq.turns.TurnNumber;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class QueueRowMapper implements RowMapper<Queue> {

    @Override
    public Queue mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");

        String initialTurnString = rs.getString("initial_turn");
        TurnNumber initialTurn = TurnNumber.from(initialTurnString);

        long branchId = rs.getLong("branch_id");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return new Queue(id, name, initialTurn, branchId, createdAt, updatedAt);
    }
}
