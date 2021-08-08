package com.jespinel.noq.reports;

import com.jespinel.noq.turns.TurnStateValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimePerStateRowMapper implements RowMapper<TimePerState> {

    @Override
    public TimePerState mapRow(ResultSet rs, int rowNum) throws SQLException {
        String stateString = rs.getString("state");
        TurnStateValue state = TurnStateValue.valueOf(stateString);

        long averageTimeInMilliseconds = rs.getLong("average_millis");

        return new TimePerState(state, averageTimeInMilliseconds);
    }
}
