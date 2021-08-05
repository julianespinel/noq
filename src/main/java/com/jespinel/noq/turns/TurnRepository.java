package com.jespinel.noq.turns;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
public class TurnRepository {

    private static final String CREATE_TURN_SQL =
            "INSERT INTO turns (turn_number, phone_number, queue_id, current_state, created_at, updated_at) " +
                    "VALUES (:turnNumber, :phoneNumber, :queueId, :currentState, :createdAt, :updatedAt)";

    private static final String FIND_TURN_BY_ID_SQL = "SELECT * FROM turns WHERE id = :id";

    private static final String GET_LATEST_TURN_BY_QUEUE_ID_SQL =
            "SELECT * FROM turns WHERE queue_id = :queueId ORDER BY id DESC LIMIT 1";

    private static final String GET_LATEST_TURN_BY_PHONE_NUMBER_SQL =
            "SELECT * FROM turns WHERE phone_number = :phoneNumber ORDER BY id DESC LIMIT 1";

    private static final String GET_OLDEST_TURN_BY_CURRENT_STATE_QUEUE_ID_SQL = """
            SELECT * FROM turns
            WHERE queue_id = :queueId
            AND current_state = :turnState
            ORDER BY id ASC LIMIT 1
            """;

    private static final String UPDATE_CURRENT_STATE_BY_ID_SQL =
            "UPDATE turns SET current_state = :currentState WHERE id = :id";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM turns";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    TurnRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Turn save(Turn turn) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("turnNumber", turn.getTurnNumber().toString())
                .addValue("phoneNumber", turn.getPhoneNumber())
                .addValue("queueId", turn.getQueueId())
                .addValue("currentState", turn.getCurrentState().toString())
                .addValue("createdAt", turn.getCreatedAt())
                .addValue("updatedAt", turn.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_TURN_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Turn(id, turn);
    }

    Turn saveOrThrow(Turn turn) {
        try {
            return save(turn);
        } catch (DuplicateKeyException e) {
            String errorMessage = "The phone number %s already has the turn %s in queue %s"
                    .formatted(turn.getPhoneNumber(), turn.getTurnNumber(), turn.getQueueId());
            throw new DuplicatedEntityException(errorMessage);
        }
    }

    Optional<Turn> getLatestTurnByQueue(long queueId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("queueId", queueId);
        List<Turn> turns = jdbcTemplate.query(GET_LATEST_TURN_BY_QUEUE_ID_SQL, params, new TurnRowMapper());
        Turn turn = DataAccessUtils.singleResult(turns);
        return Optional.ofNullable(turn);
    }

    Optional<Turn> getLatestTurnByPhoneNumber(String phoneNumber) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("phoneNumber", phoneNumber);
        List<Turn> turns = jdbcTemplate.query(GET_LATEST_TURN_BY_PHONE_NUMBER_SQL, params, new TurnRowMapper());
        Turn turn = DataAccessUtils.singleResult(turns);
        return Optional.ofNullable(turn);
    }

    /**
     * Returns the oldest turn from a queue in state 'Requested'.
     *
     * @param queueId The ID of the queue we want to call the next turn from.
     * @return The next turn to be called in a queue if exists, otherwise an empty optional.
     */
    Optional<Turn> getOldestRequestedTurn(long queueId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("queueId", queueId)
                .addValue("turnState", TurnStateValue.REQUESTED.toString());
        List<Turn> turns = jdbcTemplate.query(GET_OLDEST_TURN_BY_CURRENT_STATE_QUEUE_ID_SQL, params, new TurnRowMapper());
        Turn turn = DataAccessUtils.singleResult(turns);
        return Optional.ofNullable(turn);
    }

    int updateTurnCurrentState(long turnId, TurnStateValue newState) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", turnId)
                .addValue("currentState", newState.toString());
        return jdbcTemplate.update(UPDATE_CURRENT_STATE_BY_ID_SQL, params);
    }

    Optional<Turn> findById(long turnId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", turnId);
        List<Turn> turns = jdbcTemplate.query(FIND_TURN_BY_ID_SQL, params, new TurnRowMapper());
        Turn turn = DataAccessUtils.singleResult(turns);
        return Optional.ofNullable(turn);
    }

    Long count() {
        SqlParameterSource params = new MapSqlParameterSource();
        return jdbcTemplate.queryForObject(COUNT_SQL, params, Long.class);
    }
}
