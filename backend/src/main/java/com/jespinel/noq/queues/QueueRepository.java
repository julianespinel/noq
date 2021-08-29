package com.jespinel.noq.queues;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Repository
public class QueueRepository {

    private static final int LIMIT = 10;

    private static final String CREATE_QUEUE_SQL =
            "INSERT INTO queues (name, initial_turn, branch_id, created_at, updated_at) " +
                    "VALUES (:name, :initialTurn, :branchId, :createdAt, :updatedAt)";

    private static final String FIND_QUEUE_SQL =
            "SELECT * FROM queues WHERE id = :id";

    private static final String FIND_BY_BRANCH_ID_SQL = """
            SELECT * FROM queues
            WHERE branch_id = :branchId
            LIMIT :limit
            OFFSET :offset
            """;

    private static final String COUNT_BY_BRANCH_ID_SQL =
            "SELECT COUNT(*) FROM queues WHERE branch_id = :branchId";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    QueueRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Queue save(Queue queue) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", queue.getName())
                .addValue("initialTurn", queue.getInitialTurn().toString())
                .addValue("branchId", queue.getBranchId())
                .addValue("createdAt", queue.getCreatedAt())
                .addValue("updatedAt", queue.getUpdatedAt());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(CREATE_QUEUE_SQL, params, keyHolder, ID);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Queue(id, queue);
    }

    Queue saveOrThrow(Queue queue) {
        try {
            return save(queue);
        } catch (DuplicateKeyException e) {
            String errorMessage = "A queue with name %s from branch %s already exists"
                    .formatted(queue.getName(), queue.getBranchId());
            throw new DuplicatedEntityException(errorMessage);
        }
    }

    Optional<Queue> find(long queueId) {
        SqlParameterSource params = new MapSqlParameterSource().addValue("id", queueId);
        List<Queue> queues = jdbcTemplate.query(FIND_QUEUE_SQL, params, new QueueRowMapper());
        Queue queue = DataAccessUtils.singleResult(queues);
        return Optional.ofNullable(queue);
    }

    Page<Queue> findByBranchId(long branchId, int page) {
        int offset = LIMIT * page;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("branchId", branchId)
                .addValue("limit", LIMIT)
                .addValue("offset", offset);

        PageRequest pageable = PageRequest.of(page, LIMIT);
        List<Queue> queues = jdbcTemplate.query(FIND_BY_BRANCH_ID_SQL, params, new QueueRowMapper());
        long total = countByBranch(branchId);

        return new PageImpl<Queue>(queues, pageable, total);
    }

    Long countByBranch(long branchId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("branchId", branchId);
        return jdbcTemplate.queryForObject(COUNT_BY_BRANCH_ID_SQL, params, Long.class);
    }
}
