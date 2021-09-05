package com.jespinel.noq.branches;

import com.jespinel.noq.AbstractContainerBaseTest;
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


class BranchControllerTest extends AbstractContainerBaseTest {

    private static final String BRANCHES_URL = "/api/branches";

    @Autowired
    private BranchRepository repository;

    @Autowired
    private CompanyService companyService;

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldReturn400_WhenRequestIsNotValid() throws Exception {
        // given
        CreateBranchRequest notValidParentId = testFactories.getCreateBranchRequest(-1);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BRANCHES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notValidParentId));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given company id is not valid");
    }

    @Test
    void shouldReturn409_WhenBranchAlreadyExists() throws Exception {
        Company company = testFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);

        Branch branch = testFactories.getRandomBranch(createdCompany.getId());
        repository.save(branch);

        CreateBranchRequest duplicatedBranchRequest = testFactories.getCreateBranchRequest(branch);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BRANCHES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicatedBranchRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "A branch with name %s from company %s already exists".formatted(branch.getName(), createdCompany.getId());
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturn404_WhenParentCompanyDoesNotExist() throws Exception {
        long nonExistentCompanyId = 123;
        CreateBranchRequest nonExistentCompanyRequest = testFactories.getCreateBranchRequest(nonExistentCompanyId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BRANCHES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentCompanyRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "The company with TIN %s was not found".formatted(nonExistentCompanyId);
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturn201_WhenGivenAValidRequest() throws Exception {
        Company company = testFactories.getRandomCompany();
        Company createdCompany = companyService.create(company);
        CreateBranchRequest createBranchRequest = testFactories.getCreateBranchRequest(createdCompany.getId());
        LocalDateTime currentDate = LocalDateTime.now();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BRANCHES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBranchRequest));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Branch branch = objectMapper.readValue(response.getContentAsString(), Branch.class);
        assertThat(branch.getId()).isGreaterThan(0);
        assertThat(branch.getName()).isEqualTo(createBranchRequest.name());
        assertThat(branch.getCompanyId()).isEqualTo(createBranchRequest.companyId());
        assertThat(branch.getCreatedAt()).isAfterOrEqualTo(currentDate);
        assertThat(branch.getUpdatedAt()).isAfterOrEqualTo(currentDate);
    }
}
