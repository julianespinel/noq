package com.jespinel.noq.reports;

import com.jespinel.noq.turns.TurnStateValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CountPerStateRowMapper implements RowMapper<CountPerState> {

    @Override
    public CountPerState mapRow(ResultSet rs, int rowNum) throws SQLException {
        String stateString = rs.getString("state");
        TurnStateValue state = TurnStateValue.valueOf(stateString);
        long count = rs.getLong("count");

        return new CountPerState(state, count);
    }
}
