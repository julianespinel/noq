package com.jespinel.noq.queues;

import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.branches.Branch;
import com.jespinel.noq.branches.BranchService;
import com.jespinel.noq.common.exceptions.ApiError;
import com.jespinel.noq.companies.Company;
import com.jespinel.noq.companies.CompanyService;
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

class QueueControllerTest extends AbstractContainerBaseTest {

    private static final String QUEUES_URL = "/api/queues";

    @Autowired
    private QueueRepository repository;

    @Autowired
    private BranchService branchService;

    @Autowired
    private CompanyService companyService;

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldReturn400_WhenRequestIsNotValid() throws Exception {
        // given
        CreateQueueRequest notValidBranchId = testFactories.getCreateQueueRequest(-1);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidBranchId));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given branch id is not valid");
    }

    @Test
    void shouldReturn409_WhenQueueAlreadyExists() throws Exception {
        Company company = testFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = testFactories.getRandomBranch(createdCompany.getId());
        Branch createdBranch = branchService.create(branch);

        Queue queue = testFactories.getRandomQueue(createdBranch.getId());
        repository.save(queue);

        CreateQueueRequest duplicatedQueueRequest = testFactories.getCreateQueueRequest(queue);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicatedQueueRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "A queue with name %s from branch %s already exists".formatted(queue.getName(), createdBranch.getId());
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturn404_WhenParentBranchDoesNotExist() throws Exception {
        long nonExistentBranchId = 123;
        CreateQueueRequest nonExistentBranchRequest = testFactories.getCreateQueueRequest(nonExistentBranchId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentBranchRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "The branch with ID %s was not found".formatted(nonExistentBranchId);
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturn201_WhenGivenAValidRequest() throws Exception {
        Company company = testFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = testFactories.getRandomBranch(createdCompany.getId());
        Branch createdBranch = branchService.create(branch);

        CreateQueueRequest createQueueRequest = testFactories.getCreateQueueRequest(createdBranch.getId());
        LocalDateTime currentDate = LocalDateTime.now();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createQueueRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Queue queue = objectMapper.readValue(response.getContentAsString(), Queue.class);
        assertThat(queue.getId()).isGreaterThan(0);
        assertThat(queue.getName()).isEqualTo(createQueueRequest.name());
        assertThat(queue.getBranchId()).isEqualTo(createQueueRequest.branchId());
        assertThat(queue.getCreatedAt()).isAfterOrEqualTo(currentDate);
        assertThat(queue.getUpdatedAt()).isAfterOrEqualTo(currentDate);
    }
}
