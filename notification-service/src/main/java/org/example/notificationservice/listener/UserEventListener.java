package org.example.notificationservice.listener;

import org.example.notificationservice.dto.UserEvent;
import org.example.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);
    private final EmailService emailService;

    public UserEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-notifications", groupId = "notification-group")
    public void handleUserEvent(UserEvent event) {
        log.info("Received Kafka event: operation={}, email={}", event.getOperation(), event.getEmail());
        String subject, text;
        if ("CREATE".equals(event.getOperation())) {
            subject = "Account created";
            text = "Здравствуйте! Ваш аккаунт на сайте был успешно создан.";
        } else if ("DELETE".equals(event.getOperation())) {
            subject = "Account deleted";
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        } else {
            log.warn("Unknown operation: {}", event.getOperation());
            return;
        }
        emailService.sendEmail(event.getEmail(), subject, text);
        log.info("Email sent to {} for operation {}", event.getEmail(), event.getOperation());
    }
}