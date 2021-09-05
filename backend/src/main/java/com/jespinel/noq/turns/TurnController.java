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
    public ResponseEntity<TurnDTO> create(@RequestBody CreateTurnRequest request) {
        request.validateOrThrow();
        Turn turn = service.create(request.phoneNumber(), request.queueId());
        TurnDTO turnDTO = TurnDTO.from(turn);
        logger.debug("The turn %s was created in queue %s".formatted(turn.getTurnNumber(), turn.getQueueId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(turnDTO);
    }

    @DeleteMapping
    public ResponseEntity<TurnDTO> cancel(@RequestBody CancelTurnRequest request) {
        request.validateOrThrow();
        Turn turn = service.cancel(request.phoneNumber());
        TurnDTO turnDTO = TurnDTO.from(turn);
        logger.debug("The turn %s was cancelled in queue %s".formatted(turn.getTurnNumber(), turn.getQueueId()));
        return ResponseEntity.status(HttpStatus.OK).body(turnDTO);
    }

    @PutMapping
    public ResponseEntity<TurnDTO> callNextTurn(@RequestBody CallNextTurnRequest request) {
        request.validateOrThrow();
        Turn turn = service.callNextTurn(request.queueId());
        TurnDTO turnDTO = TurnDTO.from(turn);
        logger.debug("The turn %s was called from queue %s".formatted(turn.getTurnNumber(), turn.getQueueId()));
        return ResponseEntity.status(HttpStatus.OK).body(turnDTO);
    }

    /**
     * Endpoint used to transition a turn from one state to another.
     *
     * @param turnId The ID of the turn we want to update
     * @param request Request containing the new target state of the turn
     * @return Response entity
     */
    @PutMapping("/{turnId}")
    public ResponseEntity<TurnDTO> update(@PathVariable long turnId, @RequestBody UpdateTurnRequest request) {
        request.validateOrThrow();
        TurnStateValue targetState = TurnStateValue.valueOf(request.targetState());
        Turn turn = service.updateTurn(turnId, targetState);
        TurnDTO turnDTO = TurnDTO.from(turn);
        logger.debug("The turn %s was moved to state %s".formatted(turn.getTurnNumber(), turn.getCurrentState()));
        return ResponseEntity.status(HttpStatus.OK).body(turnDTO);
    }
}
