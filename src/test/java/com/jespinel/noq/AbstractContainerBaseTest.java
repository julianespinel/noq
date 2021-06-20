package com.jespinel.noq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.xml.bind.DatatypeConverter;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractContainerBaseTest {

    protected static final String TEST_USER_USERNAME = "qwe";
    protected static final String TEST_USER_PASSWORD = "asd";

    protected static final String TEST_POSTGRES_DB_NAME = "noqdb_test";
    protected static final String TEST_POSTGRES_USERNAME = "postgres";
    protected static final String TEST_POSTGRES_PASSWORD = "example";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    protected MockMvc mockMvc;

    // Start a single DB container per all test cases
    static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:13-alpine")
                .withDatabaseName(TEST_POSTGRES_DB_NAME)
                .withUsername(TEST_POSTGRES_USERNAME)
                .withPassword(TEST_POSTGRES_PASSWORD);
        postgres.start();
    }

    protected void cleanDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "companies");
    }

    protected void addAuthorizationHeader(MockHttpServletRequestBuilder request) {
        String authorizationHeader = "Basic " + DatatypeConverter
                .printBase64Binary((TEST_USER_USERNAME + ":" + TEST_USER_PASSWORD).getBytes());
        request.header("Authorization", authorizationHeader);
    }
}
