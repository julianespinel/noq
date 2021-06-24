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

    private static final String TEST_USER_USERNAME = "qwe";
    private static final String TEST_USER_PASSWORD = "asd";

    private static final String TEST_POSTGRES_DB_NAME = "noqdb_test";
    private static final String TEST_POSTGRES_USERNAME = "postgres";
    private static final String TEST_POSTGRES_PASSWORD = "example";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    private static final PostgreSQLContainer<?> postgres;

    // Start a single container per all test cases that extend this class.
    static {
        postgres = new PostgreSQLContainer<>("postgres:13-alpine")
                .withDatabaseName(TEST_POSTGRES_DB_NAME)
                .withUsername(TEST_POSTGRES_USERNAME)
                .withPassword(TEST_POSTGRES_PASSWORD)
                .withReuse(true);
        postgres.start();
    }

    protected void cleanDatabase() {
        String[] tables = {"branches", "companies"};
        JdbcTestUtils.deleteFromTables(jdbcTemplate, tables);
    }

    protected void addAuthorizationHeader(MockHttpServletRequestBuilder request) {
        String authorizationHeader = "Basic " + DatatypeConverter
                .printBase64Binary((TEST_USER_USERNAME + ":" + TEST_USER_PASSWORD).getBytes());
        request.header("Authorization", authorizationHeader);
    }
}
