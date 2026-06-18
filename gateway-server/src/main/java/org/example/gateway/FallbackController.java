package org.example.gateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/users")
    public Mono<String> fallbackUsers() {
        return Mono.just("User service is temporarily unavailable. Please try later.");
    }

    @RequestMapping("/fallback/notify")
    public Mono<String> fallbackNotify() {
        return Mono.just("Notification service is temporarily unavailable.");
    }
}