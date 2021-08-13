package com.jespinel.noq.reports;

import com.jespinel.noq.turns.TurnStateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final String COUNT_TURNS_BY_QUEUE_AND_DATE_RANGE = """
            SELECT state, COUNT(*) FROM turn_states ts
            JOIN turns t ON ts.turn_id = t.id
            WHERE t.queue_id = :queueId
            AND ts.created_at >= :initialDate
            AND ts.created_at <= :finalDate
            GROUP BY state
            """;

    private final String AVERAGE_STATE_TIME_DIFFERENCE_BY_QUEUE_AND_DATE_RANGE = """
            SELECT state_difference.state, avg(state_difference.millis) AS average_millis
            FROM (
                     SELECT ts.turn_id,
                            ts.state,
                            extract(epoch from (ts.created_at - lag(ts.created_at, -1)
                            OVER (ORDER BY ts.turn_id, ts.id desc))) * 1000 as millis
                     FROM turn_states ts
                              JOIN turns t ON ts.turn_id = t.id
                     WHERE t.queue_id = :queueId
                       AND ts.created_at >= :initialDate
                       AND ts.created_at <= :finalDate
                     ORDER BY ts.id asc
                 ) AS state_difference
            WHERE state_difference.millis > 0
            GROUP BY state_difference.state
            """;

    @Autowired
    public ReportRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TurnCountPerState getCountByState(long queueId, LocalDateTime initialDate, LocalDateTime finalDate) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("queueId", queueId)
                .addValue("initialDate", initialDate)
                .addValue("finalDate", finalDate);
        List<CountPerState> turnsPerState = jdbcTemplate.query(COUNT_TURNS_BY_QUEUE_AND_DATE_RANGE, params, new CountPerStateRowMapper());

        return getTurnCountPerState(turnsPerState);
    }

    private TurnCountPerState getTurnCountPerState(List<CountPerState> turnsPerState) {
        Map<TurnStateValue, Long> statesToCount = new HashMap<>();
        for (CountPerState stateCount : turnsPerState) {
            statesToCount.put(stateCount.state(), stateCount.count());
        }

        return TurnCountPerState.from(statesToCount);
    }

    public TurnTimePerState getAverageTimeByState(long queueId, LocalDateTime initialDate, LocalDateTime finalDate) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("queueId", queueId)
                .addValue("initialDate", initialDate)
                .addValue("finalDate", finalDate);
        List<TimePerState> timesPerState = jdbcTemplate.query(
                AVERAGE_STATE_TIME_DIFFERENCE_BY_QUEUE_AND_DATE_RANGE, params, new TimePerStateRowMapper());

        return getTurnTimePerState(timesPerState);
    }

    private TurnTimePerState getTurnTimePerState(List<TimePerState> timePerStateDtos) {
        Map<TurnStateValue, Long> statesToTime = new HashMap<>();
        for (TimePerState timePerStateDto : timePerStateDtos) {
            statesToTime.put(timePerStateDto.state(), timePerStateDto.milliseconds());
        }

        return TurnTimePerState.from(statesToTime);
    }
}
