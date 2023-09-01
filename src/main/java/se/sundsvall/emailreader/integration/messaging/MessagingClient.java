package se.sundsvall.emailreader.integration.messaging;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.messaging.EmailRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = MessagingConfiguration.CLIENT_ID, url = "${integration.messaging.base-url}", configuration = MessagingConfiguration.class)
@CircuitBreaker(name = MessagingConfiguration.CLIENT_ID)
public interface MessagingClient {

    @PostMapping("/email")
    ResponseEntity<Void> sendEmail(EmailRequest request);

}
