package com.jespinel.noq.turns;

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
public class TurnStateRepository {

    private static final String CREATE_TURN_STATE_SQL =
            "INSERT INTO turn_states (turn_id, state, created_at, updated_at) " +
                    "VALUES (:turnId, :state, :createdAt, :updatedAt)";

    private static final String FIND_TURN_STATE_BY_TURN_ID_SQL =
            "SELECT * FROM turn_states WHERE turn_id = :turnId";

    private static final String GET_LATEST_TURN_STATE_SQL =
            "SELECT * FROM turn_states WHERE turn_id = :turnId ORDER BY id DESC LIMIT 1";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public TurnStateRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TurnState save(TurnState turnState) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("turnId", turnState.getTurnId())
                .addValue("state", turnState.getState().toString())
                .addValue("createdAt", turnState.getCreatedAt())
                .addValue("updatedAt", turnState.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_TURN_STATE_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return TurnState.from(id, turnState);
    }

    public Optional<TurnState> findByTurnId(long turnId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("turnId", turnId);
        List<TurnState> turnStates = jdbcTemplate.query(FIND_TURN_STATE_BY_TURN_ID_SQL, params, new TurnStateRowMapper());
        TurnState turnState = DataAccessUtils.singleResult(turnStates);
        return Optional.ofNullable(turnState);
    }
}
