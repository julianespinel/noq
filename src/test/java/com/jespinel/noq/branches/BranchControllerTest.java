package com.jespinel.noq.branches;

import com.jespinel.noq.AbstractContainerBaseTest;
import com.jespinel.noq.TestFactories;
import com.jespinel.noq.common.exceptions.ApiError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;


class BranchControllerTest extends AbstractContainerBaseTest {

    private static final String CREATE_BRANCH_URL = "/api/branches";

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldReturn400_WhenRequestIsNotValid() throws Exception {
        // given
        CreateBranchRequest notValidParentId = TestFactories.getCreateBranchRequest(-1);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(CREATE_BRANCH_URL)
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
}
