package com.jespinel.noq.turns;

import com.jespinel.noq.queues.Queue;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TurnNumberService {

    final Logger logger = LoggerFactory.getLogger(TurnNumberService.class);

    private final StringRedisTemplate redisTemplate;

    public TurnNumberService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public TurnNumber getNextTurn(Queue queue) {
        long queueId = queue.getId();
        Long turn = redisTemplate.boundValueOps(String.valueOf(queueId)).increment();
        TurnNumber turnNumber = new TurnNumber(queue.getInitialTurn().letter(), turn.intValue());
        logger.debug("Created turn %s in queue %s".formatted(turnNumber.toString(), queueId));
        return turnNumber;
    }

    public void setInitialTurn(Queue queue) {
        String queueId = String.valueOf(queue.getId());
        if (Strings.isBlank(queueId)) {
            throw new IllegalArgumentException("The given queue ID is null or empty");
        }
        int initialTurn = queue.getInitialTurn().number();
        redisTemplate.boundValueOps(queueId).set(String.valueOf(initialTurn));
    }
}
