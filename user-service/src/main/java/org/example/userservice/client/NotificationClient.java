package org.example.userservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private final WebClient.Builder webClientBuilder;

    public NotificationClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackNotifyHealth")
    public String checkNotificationHealth() {
        log.info("Calling notification-service /health");
        return webClientBuilder.build()
                .get()
                .uri("http://notification-service/api/notify/health")
                .retrieve()
                .bodyToMono(String.class)
                .block(); // блокирующий вызов для простоты, в реальности лучше использовать асинхрон
    }

    private String fallbackNotifyHealth(Throwable t) {
        log.warn("Fallback: notification-service is unavailable. Error: {}", t.getMessage());
        return "Notification service is unavailable (fallback)";
    }
}