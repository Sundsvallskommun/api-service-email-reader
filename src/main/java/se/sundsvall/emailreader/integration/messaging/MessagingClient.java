package se.sundsvall.emailreader.integration.messaging;

import static se.sundsvall.emailreader.integration.messaging.MessagingConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.messaging.EmailRequest;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.base-url}", configuration = MessagingConfiguration.class)
public interface MessagingClient {

	@PostMapping("/email")
	ResponseEntity<Void> sendEmail(EmailRequest request);

}
