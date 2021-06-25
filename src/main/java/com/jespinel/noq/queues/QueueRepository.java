package com.jespinel.noq.queues;

import com.jespinel.noq.common.exceptions.DuplicatedEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;


@Repository
public class QueueRepository {

    private static final String CREATE_QUEUE_SQL =
            "INSERT INTO queues (name, branchId, created_at, updated_at) " +
                    "VALUES (:name, :branchId, :created_at, :updated_at)";

    private static final String FIND_QUEUE_SQL =
            "SELECT * FROM queues WHERE id = :id";

    private static final String[] ID = {"id"};

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    QueueRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Queue save(Queue queue) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", queue.getName())
                .addValue("branchId", queue.getBranchId())
                .addValue("created_at", queue.getCreatedAt())
                .addValue("updated_at", queue.getUpdatedAt());
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
}
