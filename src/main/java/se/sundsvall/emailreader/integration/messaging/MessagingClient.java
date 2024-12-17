package se.sundsvall.emailreader.integration.messaging;

import static se.sundsvall.emailreader.integration.messaging.MessagingConfiguration.CLIENT_ID;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.MessageResult;
import generated.se.sundsvall.messaging.SmsRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.messaging.base-url}",
	configuration = MessagingConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MessagingClient {

	@PostMapping("/{municipalityId}/email")
	ResponseEntity<Void> sendEmail(@PathVariable final String municipalityId, final EmailRequest request);

	@PostMapping("/{municipalityId}/sms")
	MessageResult sendSms(
		@RequestHeader final String origin,
		@PathVariable final String municipalityId,
		@RequestBody final SmsRequest request,
		@RequestParam final boolean async);
}
