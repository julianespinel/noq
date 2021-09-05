package com.jespinel.noq.queues;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.branches.Branch;
import com.jespinel.noq.branches.BranchService;
import com.jespinel.noq.common.exceptions.ApiError;
import com.jespinel.noq.companies.Company;
import com.jespinel.noq.companies.CompanyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    void createQueue_ShouldReturn400_WhenRequestIsNotValid() throws Exception {
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
    void createQueue_ShouldReturn409_WhenQueueAlreadyExists() throws Exception {
        Branch createdBranch = createQueueAndBranch();

        Queue queue = testFactories.getRandomQueue(createdBranch.getId());
        repository.save(queue);

        CreateQueueRequest duplicatedQueueRequest = testFactories.getCreateQueueRequest(queue);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicatedQueueRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request)
                .andReturn()
                .getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "A queue with name %s from branch %s already exists".formatted(queue.getName(), createdBranch.getId());
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void createQueue_ShouldReturn404_WhenParentBranchDoesNotExist() throws Exception {
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
    void createQueue_ShouldReturn201_WhenGivenAValidRequest() throws Exception {
        Branch createdBranch = createQueueAndBranch();

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

    @Test
    void getQueues_ShouldReturn400_GivenZeroAsBranchId() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("branchId", "0")
                .queryParam("page", "1");
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("Branch ID must be greater than 0");
    }

    @Test
    void getQueues_ShouldReturn400_GivenMinusOneAsPage() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("branchId", "1")
                .queryParam("page", "-1");
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("Page must be greater or equal to 0");
    }

    @Test
    void getQueues_ShouldReturnAnEmptyList_WhenThereAreNoQueues() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("branchId", "1")
                .queryParam("page", "0");
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Page<Queue> page = objectMapper.readValue(response.getContentAsString(), new TypeReference<PageForTests<Queue>>() {});
        assertThat(page.getContent()).isEmpty();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
        assertThat(page.getPageable().getOffset()).isZero();
        assertThat(page.getPageable().getPageNumber()).isZero();
        assertThat(page.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    void getQueues_ShouldReturnAPaginatedList_WhenThereAreQueues() throws Exception {
        Branch createdBranch = createQueueAndBranch();

        // Create one queue
        long branchId = createdBranch.getId();
        Queue queue = testFactories.getRandomQueue(branchId);
        repository.save(queue);

        MockHttpServletResponse response = getQueues(branchId, 0);
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Page<Queue> page = objectMapper.readValue(response.getContentAsString(), new TypeReference<PageForTests<Queue>>() {});
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
        assertThat(page.getPageable().getOffset()).isZero();
        assertThat(page.getPageable().getPageNumber()).isZero();
        assertThat(page.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    void getQueues_ShouldReturnMultiplePages_WhenThereAreQueues() throws Exception {
        Branch createdBranch = createQueueAndBranch();

        // Create many queues
        long branchId = createdBranch.getId();
        int numberOfQueues = 25;
        createQueues(branchId, numberOfQueues);
        // when: get first page
        MockHttpServletResponse firstResponse = getQueues(branchId, 0);
        // then
        assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Page<Queue> firstPage = objectMapper.readValue(firstResponse.getContentAsString(), new TypeReference<PageForTests<Queue>>() {});
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();
        assertThat(firstPage.getPageable().getOffset()).isZero();
        assertThat(firstPage.getPageable().getPageNumber()).isZero();
        assertThat(firstPage.getPageable().getPageSize()).isEqualTo(10);

        // when: get second page
        MockHttpServletResponse secondResponse = getQueues(branchId, 1);
        // then
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Page<Queue> secondPage = objectMapper.readValue(secondResponse.getContentAsString(), new TypeReference<PageForTests<Queue>>() {});
        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.isLast()).isFalse();
        assertThat(secondPage.getPageable().getOffset()).isEqualTo(10);
        assertThat(secondPage.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(secondPage.getPageable().getPageSize()).isEqualTo(10);

        // when: get third page
        MockHttpServletResponse thirdResponse = getQueues(branchId, 2);
        // then
        assertThat(thirdResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Page<Queue> thirdPage = objectMapper.readValue(thirdResponse.getContentAsString(), new TypeReference<PageForTests<Queue>>() {});
        assertThat(thirdPage.getContent()).hasSize(5);
        assertThat(thirdPage.isFirst()).isFalse();
        assertThat(thirdPage.isLast()).isTrue();
        assertThat(thirdPage.getPageable().getOffset()).isEqualTo(20);
        assertThat(thirdPage.getPageable().getPageNumber()).isEqualTo(2);
        assertThat(thirdPage.getPageable().getPageSize()).isEqualTo(10);
    }

    private MockHttpServletResponse getQueues(long branchId, int page) throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(QUEUES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("branchId", String.valueOf(branchId))
                .queryParam("page", String.valueOf(page));
        // when
        return mockMvc.perform(request).andReturn().getResponse();
    }

    private Branch createQueueAndBranch() {
        Company company = testFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = testFactories.getRandomBranch(createdCompany.getId());
        return branchService.create(branch);
    }

    private void createQueues(long branchId, int numberOfQueues) {
        for (int i = 0; i < numberOfQueues; i++) {
            Queue queue = testFactories.getRandomQueue(branchId);
            repository.save(queue);
        }
    }
}
