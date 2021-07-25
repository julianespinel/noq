package com.jespinel.noq.turns;


import com.jespinel.noq.notifications.NotificationService;
import com.jespinel.noq.queues.Queue;
import com.jespinel.noq.queues.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TurnService {

    final Logger logger = LoggerFactory.getLogger(TurnService.class);

    private final TurnRepository repository;
    private final QueueService queueService;
    private final TurnStateService turnStateService;
    private final NotificationService notificationService;
    private final TurnNumberService turnNumberService;

    @Autowired
    public TurnService(TurnRepository repository, QueueService queueService,
                       TurnStateService turnStateService, NotificationService notificationService,
                       TurnNumberService turnNumberService) {
        this.repository = repository;
        this.queueService = queueService;
        this.turnStateService = turnStateService;
        this.notificationService = notificationService;
        this.turnNumberService = turnNumberService;
    }

    /**
     * Create a new turn in queue.
     *
     * @param phoneNumber phone number used to take a new turn
     * @param queueId     ID of the queue where we are going to take a new turn from
     * @return A new turn in a queue.
     */
    public Turn create(String phoneNumber, long queueId) {
        Queue queue = queueService.getOrThrow(queueId);
        Turn nextTurn = generateNextTurn(phoneNumber, queue);
        Turn savedTurn = repository.saveOrThrow(nextTurn);
        turnStateService.create(savedTurn.getId());
        notificationService.notifyTurnCreation(savedTurn);
        return savedTurn;
    }

    private Turn generateNextTurn(String phoneNumber, Queue queue) {
        TurnNumber nextTurnNumber = turnNumberService.getNextTurn(queue);
        return new Turn(phoneNumber, queue.getId(), nextTurnNumber);
    }
}
