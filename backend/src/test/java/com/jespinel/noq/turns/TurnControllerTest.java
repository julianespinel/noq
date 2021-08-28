package com.jespinel.noq.turns;

import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.common.exceptions.ApiError;
import com.jespinel.noq.queues.Queue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class TurnControllerTest extends AbstractContainerBaseTest {

    private static final String TURNS_URL = "/api/turns";

    @Autowired
    private TurnService turnService;

    @Autowired
    private TurnStateRepository turnStateRepository;

    @AfterEach
    void tearDown() {
        cleanCache();
        cleanDatabase();
    }

    //--------------------------------------------------------------------------
    // Create turn
    //--------------------------------------------------------------------------

    @Test
    void createTurn_shouldReturn400_WhenPhoneNumberIsNotValid() throws Exception {
        // given
        Queue queue = testFactories.getRandomQueue();
        String phoneNumberWithErrors = "123";

        CreateTurnRequest notValidPhoneNumber = testFactories
                .getCreateTurnRequest(phoneNumberWithErrors, queue.getId());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidPhoneNumber));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given phone number is not valid");
    }

    @Test
    void createTurn_shouldReturn404_WhenParentQueueDoesNotExist() throws Exception {
        // given
        long nonExistentQueueId = 123;
        String phoneNumber = "+573002930008";

        CreateTurnRequest notValidPhoneNumber = testFactories
                .getCreateTurnRequest(phoneNumber, nonExistentQueueId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidPhoneNumber));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "The queue with ID %s was not found".formatted(nonExistentQueueId);
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void createTurn_shouldNotRepeatTurns_WhenTurnsAreCreatedInParallel() throws Exception {
        int repetitions = 5;
        int concurrent = 10;
        ExecutorService pool = Executors.newFixedThreadPool(concurrent);

        for (int repetition = 0; repetition < repetitions; repetition++) {
            List<Future<String>> turnNumbers = createTurnsConcurrently(concurrent, pool);
            checkThereAreNoDuplicatedTurns(turnNumbers);
        }
    }

    private void checkThereAreNoDuplicatedTurns(List<Future<String>> turnNumbers) throws InterruptedException, ExecutionException {
        Map<String, Boolean> existentTurns = new HashMap<>();

        for (Future<String> turnNumber : turnNumbers) {
            String currentTurn = turnNumber.get();
            boolean turnExists = existentTurns.getOrDefault(currentTurn, false);

            assertThat(turnExists).isFalse();
            existentTurns.put(currentTurn, true);
        }
    }

    private List<Future<String>> createTurnsConcurrently(int concurrent, ExecutorService pool) {
        List<Future<String>> turnNumbers = new ArrayList<>();

        int limit = 9_999;
        String basePhoneNumber = "+57300293";
        Queue queue = testFactories.createTestQueueInDB();

        for (int thread = 0; thread < concurrent; thread++) {
            String phoneNumber = basePhoneNumber + limit;
            limit--;
            CreateTurnCall parallelTest = new CreateTurnCall(phoneNumber, queue.getId());
            Future<String> turnWasCreated = pool.submit(parallelTest);
            turnNumbers.add(turnWasCreated);
        }
        return turnNumbers;
    }

    /**
     * Create turn in the given queue.
     */
    private class CreateTurnCall implements Callable<String> {

        private final String phoneNumber;
        private final long queueId;

        private CreateTurnCall(String phoneNumber, long queueId) {
            this.phoneNumber = phoneNumber;
            this.queueId = queueId;
        }

        @Override
        public String call() throws Exception {
            // given
            CreateTurnRequest notValidPhoneNumber = testFactories
                    .getCreateTurnRequest(phoneNumber, queueId);

            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TURNS_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notValidPhoneNumber));
            // when
            MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
            TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
            assertThat(turnDTO.id()).isPositive();
            assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumber);
            assertThat(turnDTO.queueId()).isEqualTo(queueId);

            return turnDTO.turnNumber();
        }
    }

    @Test
    void createTurn_shouldReturn201_WhenTurnIsCreatedInQueue() throws Exception {
        // given
        Queue createdQueue = testFactories.createTestQueueInDB();
        long queueId = createdQueue.getId();
        String phoneNumber = "+573002930008";

        CreateTurnRequest createTurnRequest = testFactories
                .getCreateTurnRequest(phoneNumber, queueId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isPositive();
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo("A1");
        assertThat(turnDTO.queueId()).isEqualTo(queueId);
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.REQUESTED);

        Optional<TurnState> optional = turnStateRepository.findLatestStateByTurnId(turnDTO.id());
        assertThat(optional).isPresent();
        TurnState turnState = optional.get();
        assertThat(turnState.getState()).isEqualTo(TurnStateValue.REQUESTED);
    }

    @Test
    void createTurn_shouldReturn201_WhenTurnIsCreatedInQueue_AndInitialTurnIsA100() throws Exception {
        // given
        String initialTurn = "A100";
        Queue createdQueue = testFactories.createTestQueueInDB(initialTurn);
        long queueId = createdQueue.getId();
        String phoneNumber = "+573002930008";

        CreateTurnRequest createTurnRequest = testFactories
                .getCreateTurnRequest(phoneNumber, queueId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isPositive();
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo("A101");
        assertThat(turnDTO.queueId()).isEqualTo(queueId);
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.REQUESTED);
    }

    //--------------------------------------------------------------------------
    // Cancel turn
    //--------------------------------------------------------------------------

    @Test
    void cancelTurn_shouldReturn400_WhenPhoneNumberIsNotValid() throws Exception {
        // given
        String phoneNumberWithErrors = "123";
        CancelTurnRequest notValidPhoneNumber = new CancelTurnRequest(phoneNumberWithErrors);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidPhoneNumber));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given phone number is not valid");
    }

    @Test
    void cancelTurn_shouldReturn404_WhenThePhoneNumberDoesNotHaveAssociatedTurns() throws Exception {
        // given
        String validPhoneNumber = "+573002930008";
        CancelTurnRequest cancelTurnRequest = new CancelTurnRequest(validPhoneNumber);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo(validPhoneNumber + " does not have associated turns");
    }

    @Test
    void cancelTurn_shouldReturn409_WhenTurnCannotBeCancelled() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        String validPhoneNumber = "+573002930008";
        Turn existentTurn = turnService.create(validPhoneNumber, queue.getId());

        // Change turn state from requested to started
        Optional<TurnState> turnState = turnStateRepository.findLatestStateByTurnId(existentTurn.getId());
        TurnState requested = turnState.get();
        LocalDateTime now = LocalDateTime.now();
        TurnState started = new TurnState(requested.getId(), requested.getTurnId(), TurnStateValue.STARTED, now, now);
        turnStateRepository.save(started);

        CancelTurnRequest cancelTurnRequest = new CancelTurnRequest(validPhoneNumber);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).contains("can't transition from STARTED to CANCELLED");
    }

    @Test
    void cancelTurn_shouldReturn200_WhenThePhoneNumberHasAssociatedTurns() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        String validPhoneNumber = "+573002930008";
        Turn existentTurn = turnService.create(validPhoneNumber, queue.getId());

        CancelTurnRequest cancelTurnRequest = new CancelTurnRequest(validPhoneNumber);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isEqualTo(existentTurn.getId());
        assertThat(turnDTO.phoneNumber()).isEqualTo(validPhoneNumber);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo(existentTurn.getTurnNumber().toString());
        assertThat(turnDTO.queueId()).isEqualTo(queue.getId());
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.CANCELLED);

        Optional<TurnState> optional = turnStateRepository.findLatestStateByTurnId(turnDTO.id());
        assertThat(optional).isPresent();
        TurnState turnState = optional.get();
        assertThat(turnState.getState()).isEqualTo(TurnStateValue.CANCELLED);
    }

    @Test
    void cancelTurn_shouldReturn200AndCallNextTurn_WhenCancellingTurnInReadyState() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        Turn turnOne = turnService.create(phoneNumberOne, queueId);
        turnService.callNextTurn(queueId);// Set turn state to ready

        String phoneNumberTwo = "+573002930002";
        Turn turnTwo = turnService.create(phoneNumberTwo, queueId);// Current turn state is requested

        CancelTurnRequest cancelTurnRequest = new CancelTurnRequest(phoneNumberOne);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        // Check turnOne is cancelled
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isEqualTo(turnOne.getId());
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumberOne);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo(turnOne.getTurnNumber().toString());
        assertThat(turnDTO.queueId()).isEqualTo(queue.getId());
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.CANCELLED);

        // Check turnTwo is ready
        // turnTwo should have been called when turnOne (in ready state) was cancelled
        Turn ready = turnService.getOrThrow(turnTwo.getId());
        assertThat(ready.getPhoneNumber()).isEqualTo(phoneNumberTwo);
        assertThat(ready.getCurrentState()).isEqualTo(TurnStateValue.READY);

        // turnTwo latest state should be ready
        Optional<TurnState> optional = turnStateRepository.findLatestStateByTurnId(ready.getId());
        assertThat(optional).isNotEmpty();
        TurnState turnTwoState = optional.get();
        assertThat(turnTwoState.getState()).isEqualTo(TurnStateValue.READY);
    }

    //--------------------------------------------------------------------------
    // Call next turn
    //--------------------------------------------------------------------------

    @Test
    void callNextTurn_shouldReturn400_whenGivenQueueIdIsNotValid() throws Exception {
        // given
        CallNextTurnRequest notValidQueueIdRequest = new CallNextTurnRequest(-1);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidQueueIdRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given queue id is not valid");
    }

    @Test
    void callNextTurn_shouldReturn409_whenQueueIsEmpty() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        CallNextTurnRequest callNextTurnRequest = new CallNextTurnRequest(queue.getId());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callNextTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The queue is empty, there are no turns to call");
    }

    @Test
    void callNextTurn_shouldReturn200_whenQueueHasASingleRequestedTurn() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        turnService.create(phoneNumberOne, queueId);
        turnService.callNextTurn(queueId);// Set turn state to ready

        String phoneNumberTwo = "+573002930002";
        turnService.create(phoneNumberTwo, queueId);
        turnService.callNextTurn(queueId);// Set turn state to ready

        String phoneNumberThree = "+573002930003";
        Turn requestedTurn = turnService.create(phoneNumberThree, queueId);
        // Only turn in requested state ^

        CallNextTurnRequest callNextTurnRequest = new CallNextTurnRequest(queueId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callNextTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isEqualTo(requestedTurn.getId());
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumberThree);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo(requestedTurn.getTurnNumber().toString());
        assertThat(turnDTO.queueId()).isEqualTo(queueId);
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.READY);
    }

    @Test
    void callNextTurn_shouldReturn200AndTheOldestTurnInRequestState_whenQueueHasManyRequestedTurns() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        Turn firstRequestedTurn = turnService.create(phoneNumberOne, queueId);
        // Turn in requested state ^

        String phoneNumberTwo = "+573002930002";
        turnService.create(phoneNumberTwo, queueId);
        // Turn in requested state ^

        String phoneNumberThree = "+573002930003";
        turnService.create(phoneNumberThree, queueId);
        // Turn in requested state ^

        CallNextTurnRequest callNextTurnRequest = new CallNextTurnRequest(queueId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(TURNS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callNextTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isEqualTo(firstRequestedTurn.getId());
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumberOne);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo(firstRequestedTurn.getTurnNumber().toString());
        assertThat(turnDTO.queueId()).isEqualTo(queueId);
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.READY);

        Optional<TurnState> optional = turnStateRepository.findLatestStateByTurnId(turnDTO.id());
        assertThat(optional).isPresent();
        TurnState turnState = optional.get();
        assertThat(turnState.getState()).isEqualTo(TurnStateValue.READY);
    }

    //--------------------------------------------------------------------------
    // Start turn
    //--------------------------------------------------------------------------

    @Test
    void startTurn_shouldReturn400_whenTheGivenRequestIsNotValid() throws Exception {
        // given
        String unknownState = "unknown";
        UpdateTurnRequest updateTurnRequest = new UpdateTurnRequest(unknownState);
        String url = TURNS_URL + "/" + 1;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given target state is not valid");
    }

    @Test
    void startTurn_shouldReturn409_whenTheTurnIsNotInReadyState() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        Turn requestedTurn = turnService.create(phoneNumberOne, queueId);
        // Turn in requested state ^

        UpdateTurnRequest updateTurnRequest = new UpdateTurnRequest(TurnStateValue.STARTED.toString());
        String url = TURNS_URL + "/" + requestedTurn.getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "Turn %s can't transition from %s to %s"
                .formatted(requestedTurn.getId(), TurnStateValue.REQUESTED, TurnStateValue.STARTED);
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void startTurn_shouldReturn200_whenTheGivenTurnIsInReadyState() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        turnService.create(phoneNumberOne, queueId);
        Turn readyTurn = turnService.callNextTurn(queueId);

        UpdateTurnRequest updateTurnRequest = new UpdateTurnRequest(TurnStateValue.STARTED.toString());
        String url = TURNS_URL + "/" + readyTurn.getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isEqualTo(readyTurn.getId());
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumberOne);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo(readyTurn.getTurnNumber().toString());
        assertThat(turnDTO.queueId()).isEqualTo(queueId);
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.STARTED);

        Optional<TurnState> optional = turnStateRepository.findLatestStateByTurnId(turnDTO.id());
        assertThat(optional).isPresent();
        TurnState turnState = optional.get();
        assertThat(turnState.getState()).isEqualTo(TurnStateValue.STARTED);
    }

    //--------------------------------------------------------------------------
    // End turn
    //--------------------------------------------------------------------------

    @Test
    void startTurn_shouldReturn409_whenTheTurnIsNotInStartedState() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        Turn requestedTurn = turnService.create(phoneNumberOne, queueId);
        // Turn in requested state ^

        UpdateTurnRequest updateTurnRequest = new UpdateTurnRequest(TurnStateValue.ENDED.toString());
        String url = TURNS_URL + "/" + requestedTurn.getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "Turn %s can't transition from %s to %s"
                .formatted(requestedTurn.getId(), TurnStateValue.REQUESTED, TurnStateValue.ENDED);
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void startTurn_shouldReturn200_whenTheGivenTurnIsInStartedState() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();

        String phoneNumberOne = "+573002930001";
        turnService.create(phoneNumberOne, queueId);
        Turn readyTurn = turnService.callNextTurn(queueId);
        Turn startedTurn = turnService.updateTurn(readyTurn.getId(), TurnStateValue.STARTED);

        UpdateTurnRequest updateTurnRequest = new UpdateTurnRequest(TurnStateValue.ENDED.toString());
        String url = TURNS_URL + "/" + startedTurn.getId();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTurnRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        TurnDTO turnDTO = objectMapper.readValue(response.getContentAsString(), TurnDTO.class);
        assertThat(turnDTO.id()).isEqualTo(startedTurn.getId());
        assertThat(turnDTO.phoneNumber()).isEqualTo(phoneNumberOne);
        assertThat(turnDTO.turnNumber().toString()).isEqualTo(startedTurn.getTurnNumber().toString());
        assertThat(turnDTO.queueId()).isEqualTo(queueId);
        assertThat(turnDTO.currentState()).isEqualTo(TurnStateValue.ENDED);

        Optional<TurnState> optional = turnStateRepository.findLatestStateByTurnId(turnDTO.id());
        assertThat(optional).isPresent();
        TurnState turnState = optional.get();
        assertThat(turnState.getState()).isEqualTo(TurnStateValue.ENDED);
    }
}
