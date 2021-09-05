package com.jespinel.noq.reports;

import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.common.exceptions.ApiError;
import com.jespinel.noq.queues.Queue;
import com.jespinel.noq.turns.Turn;
import com.jespinel.noq.turns.TurnService;
import com.jespinel.noq.turns.TurnStateValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReportControllerTest extends AbstractContainerBaseTest {

    private static final String REPORTS_URL = "/api/reports";

    @Autowired
    private TurnService turnService;

    @AfterEach
    void tearDown() {
        cleanCache();
        cleanDatabase();
    }

    @Test
    void generateGeneralReport_ShouldReturn400_WhenGivenDateRangeIsNotValid() throws Exception {
        // given
        long queueId = 1;
        LocalDateTime initialDate = LocalDateTime.now();
        LocalDateTime finalDate = initialDate.plusDays(1);
        ReportRequest notValidRequest = new ReportRequest(queueId, finalDate, initialDate);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORTS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given initial date should be before the final date");
    }

    @Test
    void generateGeneralReport_ShouldReturn200AndAnEmptyReport_WhenDBIsEmpty() throws Exception {
        // given
        long queueId = 1;
        LocalDateTime initialDate = LocalDateTime.now();
        LocalDateTime finalDate = initialDate.plusDays(1);
        ReportRequest reportRequest = new ReportRequest(queueId, initialDate, finalDate);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORTS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        GeneralReport generalReport = objectMapper.readValue(response.getContentAsString(), GeneralReport.class);
        assertThat(generalReport).isNotNull();

        TurnCountPerState countPerState = generalReport.countPerState();
        assertThat(countPerState.requestedTurns()).isEqualTo(-1);
        assertThat(countPerState.readyTurns()).isEqualTo(-1);
        assertThat(countPerState.cancelledTurns()).isEqualTo(-1);
        assertThat(countPerState.startedTurns()).isEqualTo(-1);
        assertThat(countPerState.endedTurns()).isEqualTo(-1);

        TurnTimePerState timePerState = generalReport.timePerState();
        assertThat(timePerState.averageCustomerWaitingTimeInSeconds()).isEqualTo(-1);
        assertThat(timePerState.averageCompanyWaitingTimeInSeconds()).isEqualTo(-1);
        assertThat(timePerState.averageTurnCancellationTimeInSeconds()).isEqualTo(-1);
        assertThat(timePerState.averageTurnExecutionTimeInSeconds()).isEqualTo(-1);
    }

    @Test
    void generateGeneralReport_ShouldReturn200_WhenThereIsOnlyOneTurnInTheDB() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();
        LocalDateTime initialDate = LocalDateTime.now();
        LocalDateTime finalDate = initialDate.plusDays(1);
        ReportRequest reportRequest = new ReportRequest(queueId, initialDate, finalDate);

        int timeBetweenStates = 300;
        String phoneNumberOne = "+573002930001";
        createAndCompleteTurn(phoneNumberOne, queueId, timeBetweenStates);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORTS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        GeneralReport generalReport = objectMapper.readValue(response.getContentAsString(), GeneralReport.class);
        assertThat(generalReport).isNotNull();

        TurnCountPerState countPerState = generalReport.countPerState();
        assertThat(countPerState.requestedTurns()).isEqualTo(1);
        assertThat(countPerState.readyTurns()).isEqualTo(1);
        assertThat(countPerState.cancelledTurns()).isEqualTo(-1);
        assertThat(countPerState.startedTurns()).isEqualTo(1);
        assertThat(countPerState.endedTurns()).isEqualTo(1);

        int errorMargin = 30;
        TurnTimePerState timePerState = generalReport.timePerState();
        assertValueIsWithinErrorMargin(timePerState.averageCustomerWaitingTimeInSeconds(), timeBetweenStates, errorMargin);
        assertValueIsWithinErrorMargin(timePerState.averageCompanyWaitingTimeInSeconds(), timeBetweenStates, errorMargin);
        assertThat(timePerState.averageTurnCancellationTimeInSeconds()).isEqualTo(-1);
        // The turn was not cancelled ^
        assertValueIsWithinErrorMargin(timePerState.averageTurnExecutionTimeInSeconds(), timeBetweenStates, errorMargin);
    }

    @Test
    void generateGeneralReport_ShouldReturn200_WhenThereAreThreeTurnsInTheDB() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();
        LocalDateTime initialDate = LocalDateTime.now();
        LocalDateTime finalDate = initialDate.plusDays(1);
        ReportRequest reportRequest = new ReportRequest(queueId, initialDate, finalDate);

        int timesTurnOne = 300;
        String phoneNumberOne = "+573002930001";
        createAndCompleteTurn(phoneNumberOne, queueId, timesTurnOne);

        int timesTurnTwo = 600;
        String phoneNumberTwo = "+573002930002";
        createAndCompleteTurn(phoneNumberTwo, queueId, timesTurnTwo);

        int timesTurnThree = 900;
        String phoneNumberThree = "+573002930003";
        createAndCompleteTurn(phoneNumberThree, queueId, timesTurnThree);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORTS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        GeneralReport generalReport = objectMapper.readValue(response.getContentAsString(), GeneralReport.class);
        assertThat(generalReport).isNotNull();

        TurnCountPerState countPerState = generalReport.countPerState();
        assertThat(countPerState.requestedTurns()).isEqualTo(3);
        assertThat(countPerState.readyTurns()).isEqualTo(3);
        assertThat(countPerState.cancelledTurns()).isEqualTo(-1);
        assertThat(countPerState.startedTurns()).isEqualTo(3);
        assertThat(countPerState.endedTurns()).isEqualTo(3);

        int averageTime = (timesTurnOne + timesTurnTwo + timesTurnThree) / 3;
        int errorMargin = 30;
        TurnTimePerState timePerState = generalReport.timePerState();
        assertValueIsWithinErrorMargin(timePerState.averageCustomerWaitingTimeInSeconds(), averageTime, errorMargin);
        assertValueIsWithinErrorMargin(timePerState.averageCompanyWaitingTimeInSeconds(), averageTime, errorMargin);
        assertThat(timePerState.averageTurnCancellationTimeInSeconds()).isEqualTo(-1);
        assertValueIsWithinErrorMargin(timePerState.averageTurnExecutionTimeInSeconds(), averageTime, errorMargin);
    }

    @Test
    void generateGeneralReport_ShouldReturn200_WhenThereAreFourTurnsInTheDB() throws Exception {
        // given
        Queue queue = testFactories.createTestQueueInDB();
        long queueId = queue.getId();
        LocalDateTime initialDate = LocalDateTime.now();
        LocalDateTime finalDate = initialDate.plusDays(1);
        ReportRequest reportRequest = new ReportRequest(queueId, initialDate, finalDate);

        int timesTurnOne = 300;
        String phoneNumberOne = "+573002930001";
        createAndCompleteTurn(phoneNumberOne, queueId, timesTurnOne);

        int timesTurnTwo = 600;
        String phoneNumberTwo = "+573002930002";
        createAndCompleteTurn(phoneNumberTwo, queueId, timesTurnTwo);

        int timesTurnThree = 900;
        String phoneNumberThree = "+573002930003";
        createAndCompleteTurn(phoneNumberThree, queueId, timesTurnThree);

        int timesTurnFour = 1200;
        String phoneNumberFour = "+573002930004";
        createAndCancelTurn(phoneNumberFour, queueId, timesTurnFour);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REPORTS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        GeneralReport generalReport = objectMapper.readValue(response.getContentAsString(), GeneralReport.class);
        assertThat(generalReport).isNotNull();

        TurnCountPerState countPerState = generalReport.countPerState();
        assertThat(countPerState.requestedTurns()).isEqualTo(4);
        assertThat(countPerState.readyTurns()).isEqualTo(3);
        assertThat(countPerState.cancelledTurns()).isEqualTo(1);
        // Only one turn was cancelled ^
        assertThat(countPerState.startedTurns()).isEqualTo(3);
        assertThat(countPerState.endedTurns()).isEqualTo(3);

        int averageTime = (timesTurnOne + timesTurnTwo + timesTurnThree) / 3;
        int errorMargin = 30;
        TurnTimePerState timePerState = generalReport.timePerState();
        assertValueIsWithinErrorMargin(timePerState.averageCustomerWaitingTimeInSeconds(), averageTime, errorMargin);
        assertValueIsWithinErrorMargin(timePerState.averageCompanyWaitingTimeInSeconds(), averageTime, errorMargin);
        assertValueIsWithinErrorMargin(timePerState.averageTurnCancellationTimeInSeconds(), timesTurnFour, errorMargin);
        // Only one turn was cancelled ^
        assertValueIsWithinErrorMargin(timePerState.averageTurnExecutionTimeInSeconds(), averageTime, errorMargin);
    }

    private Turn createAndCompleteTurn(String phoneNumber, long queueId, int timeBetweenStates) throws InterruptedException {
        Turn turn = turnService.create(phoneNumber, queueId);
        Thread.sleep(timeBetweenStates);
        turnService.callNextTurn(queueId);
        Thread.sleep(timeBetweenStates);
        turnService.updateTurn(turn.getId(), TurnStateValue.STARTED);
        Thread.sleep(timeBetweenStates);
        return turnService.updateTurn(turn.getId(), TurnStateValue.ENDED);
    }

    private Turn createAndCancelTurn(String phoneNumber, long queueId, int timeBetweenStates) throws InterruptedException {
        Turn turn = turnService.create(phoneNumber, queueId);
        Thread.sleep(timeBetweenStates);
        return turnService.cancel(turn.getPhoneNumber());
    }

    private void assertValueIsWithinErrorMargin(long value, long expectedValue, long errorMargin) {
        assertThat(value).isBetween(expectedValue - errorMargin, expectedValue + errorMargin);
    }
}
