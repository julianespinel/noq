package com.jespinel.noq.turns;

import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.common.exceptions.TurnStateTransitionException;
import com.jespinel.noq.queues.Queue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnServiceTest extends AbstractContainerBaseTest {

    @MockBean
    TurnStateService turnStateServiceMock;

    @Autowired
    private TurnService turnService;

    @Autowired
    private TurnRepository turnRepository;

    @AfterEach
    void tearDown() {
        cleanCache();
        cleanDatabase();
    }

    @Test
    void createTurn_shouldRollbackTransaction_WhenTurnStateCreationFails() {
        // given
        Queue createdQueue = testFactories.createTestQueueInDB();
        long queueId = createdQueue.getId();
        String phoneNumber = "+573002930008";

        // This will make the creation transaction fail.
        int turnId = 1;
        RuntimeException mockedException = new RuntimeException("Mocked exception from test");
        Mockito.when(turnStateServiceMock.create(turnId)).thenThrow(mockedException);

        // when
        assertThrows(RuntimeException.class, () -> {
            turnService.create(phoneNumber, queueId);
        });

        // then
        // Check the transaction was roll-backed and the turn does not exist.
        assertThat(turnRepository.count()).isZero();
    }

    @Test
    void cancelTurn_shouldRollbackTransaction_WhenTurnStateCreationFails() {
        // given
        Queue createdQueue = testFactories.createTestQueueInDB();
        long queueId = createdQueue.getId();
        String phoneNumber = "+573002930008";

        // This will make the creation transaction fail.
        TurnStateTransitionException mockedException = new TurnStateTransitionException("Mocked exception from test");

        // Create turn first
        Turn createdTurn = turnService.create(phoneNumber, queueId);
        long turnId = createdTurn.getId();

        Mockito.when(
                turnStateServiceMock.moveToState(turnId, TurnStateValue.CANCELLED)
        ).thenThrow(mockedException);

        // when
        assertThrows(TurnStateTransitionException.class, () -> {
            turnService.cancel(phoneNumber);
        });

        // then
        // Check the transaction was roll-backed and the turn does not exist.
        // Check turn
        Optional<Turn> optionalTurn = turnRepository.findById(turnId);
        assertThat(optionalTurn).isNotEmpty();

        Turn turn = optionalTurn.get();
        assertThat(turn.getId()).isEqualTo(turnId);

        // It is REQUESTED because the transaction was roll-backed.
        assertThat(turn.getCurrentState()).isEqualTo(TurnStateValue.REQUESTED);
    }
}
