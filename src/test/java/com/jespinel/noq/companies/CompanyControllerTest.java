package com.jespinel.noq.companies;

import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.common.exceptions.ApiError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;


class CompanyControllerTest extends AbstractContainerBaseTest {

    private static final String COMPANIES_URL = "/api/companies";

    @Autowired
    private CompanyRepository repository;

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldReturn400_WhenRequestIsNotValid() throws Exception {
        // given
        CreateCompanyRequest emptyNitCompany = new CreateCompanyRequest("", "name");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(COMPANIES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyNitCompany));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(apiError.error()).isEqualTo("The given nit was null or empty");
    }

    @Test
    void shouldReturn409_WhenCompanyWithSameNitIsAlreadyCreated() throws Exception {
        // given
        Company company = createRandomCompanyInDB();
        CreateCompanyRequest duplicatedCompany = new CreateCompanyRequest(company.getNit(), company.getName());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(COMPANIES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicatedCompany));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        ApiError apiError = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        String errorMessage = "A company with nit %s already exists".formatted(company.getNit());
        assertThat(apiError.error()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturn201_WhenRequestIsValid() throws Exception {
        Company company = testFactories.getRandomCompany();
        CreateCompanyRequest newCompany = new CreateCompanyRequest(company.getNit(), company.getName());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(COMPANIES_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompany));
        // when
        MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Company createdCompany = objectMapper.readValue(response.getContentAsString(), Company.class);
        assertThat(createdCompany.getId()).isGreaterThan(0);
        assertThat(createdCompany.getNit()).isEqualTo(company.getNit());
        assertThat(createdCompany.getName()).isEqualTo(company.getName());
        assertThat(createdCompany.getCreatedAt()).isAfterOrEqualTo(company.getCreatedAt());
        assertThat(createdCompany.getUpdatedAt()).isAfterOrEqualTo(company.getUpdatedAt());
    }

    // private methods

    private Company createRandomCompanyInDB() {
        Company company = testFactories.getRandomCompany();
        return repository.save(company);
    }
}
