package com.jespinel.noq.turns;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turns")
public class TurnController {

    final Logger logger = LoggerFactory.getLogger(TurnController.class);

    private final TurnService service;

    @Autowired
    public TurnController(TurnService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Turn> create(@RequestBody CreateTurnRequest request) {
        request.validateOrThrow();
        Turn turn = service.create(request.phoneNumber(), request.queueId());
        logger.debug("The turn %s was created in the queue %s".formatted(turn.getTurnNumber(), turn.getQueueId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(turn);
    }

    @DeleteMapping
    public ResponseEntity<Turn> cancel(@RequestBody CancelTurnRequest request) {
        request.validateOrThrow();
        Turn turn = service.cancel(request.phoneNumber());
        logger.debug("The turn %s was cancelled in the queue %s".formatted(turn.getTurnNumber(), turn.getQueueId()));
        return ResponseEntity.status(HttpStatus.OK).body(turn);
    }

    @PutMapping
    public ResponseEntity<Turn> callNextTurn(@RequestBody CallNextTurnRequest request) {
        request.validateOrThrow();
        Turn turn = service.callNextTurn(request.queueId());
        return ResponseEntity.status(HttpStatus.OK).body(turn);
    }
}
