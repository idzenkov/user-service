package org.example.notificationservice;

import org.example.notificationservice.dto.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class NotificationIntegrationTest {

    @Container
    static GenericContainer<?> greenMail = new GenericContainer<>(DockerImageName.parse("mailhog/mailhog:latest"))
            .withExposedPorts(1025, 8025)
            .waitingFor(Wait.forLogMessage(".*Serving under http.*", 1))
            .withStartupTimeout(Duration.ofSeconds(30));

    @Container
    static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("apache/kafka:3.7.0"))
            .withExposedPorts(9092)
            .withEnv("KAFKA_NODE_ID", "1")
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@localhost:9093")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
            .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
            .withEnv("KAFKA_LOG_DIRS", "/tmp/kraft-combined-logs")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withCommand("sh", "-c",
                    "/opt/kafka/bin/kafka-storage.sh format -t $(/opt/kafka/bin/kafka-storage.sh random-uuid) -c /opt/kafka/config/kraft/server.properties && " +
                            "exec /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/kraft/server.properties")
            .waitingFor(Wait.forLogMessage(".*Kafka Server started.*", 1))
            .withStartupTimeout(Duration.ofMinutes(4));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> greenMail.getMappedPort(1025));
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:" + kafka.getMappedPort(9092));
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldSendEmailOnUserEvent() throws Exception {
        UserEvent event = new UserEvent("CREATE", "test@example.com");
        kafkaTemplate.send("user-notifications", event);

        Thread.sleep(5000);

        String mailhogApiUrl = "http://localhost:" + greenMail.getMappedPort(8025) + "/api/v2/messages";
        String responseBody = restTemplate.getForObject(mailhogApiUrl, String.class);
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        assertThat(items).isNotEmpty();

        Map<String, Object> firstMessage = items.get(0);
        Map<String, Object> content = (Map<String, Object>) firstMessage.get("Content");
        Map<String, Object> headers = (Map<String, Object>) content.get("Headers");
        List<String> to = (List<String>) headers.get("To");
        assertThat(to.get(0)).contains("test@example.com");

        List<String> subject = (List<String>) headers.get("Subject");
        assertThat(subject.get(0)).isEqualTo("Account created");
    }
}