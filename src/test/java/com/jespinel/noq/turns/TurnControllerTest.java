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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class TurnControllerTest extends AbstractContainerBaseTest {

    private static final String TURNS_URL = "/api/turns";

    @Autowired
    private TurnService turnService;

    @AfterEach
    void tearDown() {
        cleanCache();
        cleanDatabase();
    }

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
            List<Future<TurnNumber>> turnNumbers = createTurnsConcurrently(concurrent, pool);
            checkThereAreNoDuplicatedTurns(turnNumbers);
        }
    }

    private void checkThereAreNoDuplicatedTurns(List<Future<TurnNumber>> turnNumbers) throws InterruptedException, ExecutionException {
        Map<String, Boolean> existentTurns = new HashMap<>();

        for (Future<TurnNumber> turnNumber : turnNumbers) {
            String currentTurn = turnNumber.get().toString();
            boolean turnExists = existentTurns.getOrDefault(currentTurn, false);

            assertThat(turnExists).isFalse();
            existentTurns.put(currentTurn, true);
        }
    }

    private List<Future<TurnNumber>> createTurnsConcurrently(int concurrent, ExecutorService pool) {
        List<Future<TurnNumber>> turnNumbers = new ArrayList<>();

        int limit = 9_999;
        String basePhoneNumber = "+57300293";
        Queue queue = testFactories.createTestQueueInDB();

        for (int thread = 0; thread < concurrent; thread++) {
            String phoneNumber = basePhoneNumber + limit;
            limit--;
            CreateTurnCall parallelTest = new CreateTurnCall(phoneNumber, queue.getId());
            Future<TurnNumber> turnWasCreated = pool.submit(parallelTest);
            turnNumbers.add(turnWasCreated);
        }
        return turnNumbers;
    }

    /**
     * Create turn in the given queue.
     */
    private class CreateTurnCall implements Callable<TurnNumber> {

        private final String phoneNumber;
        private final long queueId;

        private CreateTurnCall(String phoneNumber, long queueId) {
            this.phoneNumber = phoneNumber;
            this.queueId = queueId;
        }

        @Override
        public TurnNumber call() throws Exception {
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
            Turn turn = objectMapper.readValue(response.getContentAsString(), Turn.class);
            assertThat(turn.getId()).isPositive();
            assertThat(turn.getPhoneNumber()).isEqualTo(phoneNumber);
            assertThat(turn.getQueueId()).isEqualTo(queueId);

            return turn.getTurnNumber();
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
        Turn turn = objectMapper.readValue(response.getContentAsString(), Turn.class);
        assertThat(turn.getId()).isPositive();
        assertThat(turn.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(turn.getTurnNumber().toString()).isEqualTo("A1");
        assertThat(turn.getQueueId()).isEqualTo(queueId);
        assertThat(turn.getCurrentState()).isEqualTo(TurnStateValue.REQUESTED);
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
        Turn turn = objectMapper.readValue(response.getContentAsString(), Turn.class);
        assertThat(turn.getId()).isPositive();
        assertThat(turn.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(turn.getTurnNumber().toString()).isEqualTo("A101");
        assertThat(turn.getQueueId()).isEqualTo(queueId);
        assertThat(turn.getCurrentState()).isEqualTo(TurnStateValue.REQUESTED);
    }

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
    void cancelTurn_shouldReturn200AndNoBody_WhenThePhoneNumberDoesNotHaveAnAssociatedTurn() throws Exception {
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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void cancelTurn_shouldReturn200WithBody_WhenThePhoneNumberHasAnAssociatedTurn() throws Exception {
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
        Turn turn = objectMapper.readValue(response.getContentAsString(), Turn.class);
        assertThat(turn.getId()).isEqualTo(existentTurn.getId());
        assertThat(turn.getPhoneNumber()).isEqualTo(validPhoneNumber);
        assertThat(turn.getTurnNumber().toString()).isEqualTo(existentTurn.getTurnNumber().toString());
        assertThat(turn.getQueueId()).isEqualTo(queue.getId());
    }

    private Queue createQueue() {
        Company company = TestFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = TestFactories.getRandomBranch(createdCompany.getId());
        Branch createdBranch = branchService.create(branch);

        Queue queue = TestFactories.getRandomQueue(createdBranch.getId());
        return queueService.create(queue);
    }

    private Queue createQueue(String initialTurn) {
        Company company = TestFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = TestFactories.getRandomBranch(createdCompany.getId());
        Branch createdBranch = branchService.create(branch);

        Queue queue = TestFactories.getRandomQueue(createdBranch.getId(), initialTurn);
        return queueService.create(queue);
    }
}
