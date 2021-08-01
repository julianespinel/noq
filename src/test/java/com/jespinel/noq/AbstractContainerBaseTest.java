package com.jespinel.noq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.xml.bind.DatatypeConverter;
import java.util.Objects;


@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractContainerBaseTest {

    private static final String TEST_USER_USERNAME = "qwe";
    private static final String TEST_USER_PASSWORD = "asd";

    private static final String TEST_POSTGRES_DB_NAME = "noqdb_test";
    private static final String TEST_POSTGRES_USERNAME = "postgres";
    private static final String TEST_POSTGRES_PASSWORD = "example";

    private static final int REDIS_PORT = 6379;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    protected TestFactories testFactories;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    private static final PostgreSQLContainer<?> postgres;

    private static final GenericContainer<?> redisContainer;

    // Start a single container per all test cases that extend this class.
    static {
        postgres = new PostgreSQLContainer<>("postgres:13-alpine")
                .withDatabaseName(TEST_POSTGRES_DB_NAME)
                .withUsername(TEST_POSTGRES_USERNAME)
                .withPassword(TEST_POSTGRES_PASSWORD)
                .withReuse(true);
        postgres.start();

        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6-alpine"))
                .withExposedPorts(REDIS_PORT);
        redisContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getContainerIpAddress);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
    }

    protected void cleanDatabase() {
        String[] tables = {"turn_states", "turns", "queues", "branches", "companies"};
        JdbcTestUtils.deleteFromTables(jdbcTemplate, tables);
    }

    protected void cleanCache() {
        RedisConnectionFactory connectionFactory =
                Objects.requireNonNull(redisTemplate.getConnectionFactory());
        connectionFactory.getConnection().flushAll();
    }

    protected void addAuthorizationHeader(MockHttpServletRequestBuilder request) {
        String authorizationHeader = "Basic " + DatatypeConverter
                .printBase64Binary((TEST_USER_USERNAME + ":" + TEST_USER_PASSWORD).getBytes());
        request.header("Authorization", authorizationHeader);
    }
}
