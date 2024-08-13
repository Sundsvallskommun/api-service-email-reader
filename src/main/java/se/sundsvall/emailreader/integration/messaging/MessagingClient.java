package se.sundsvall.emailreader.integration.messaging;

import static se.sundsvall.emailreader.integration.messaging.MessagingConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import generated.se.sundsvall.messaging.EmailRequest;
import io.swagger.v3.oas.annotations.Parameter;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.base-url}", configuration = MessagingConfiguration.class)
public interface MessagingClient {

	@PostMapping("/{municipalityId}/email")
	ResponseEntity<Void> sendEmail(@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId, EmailRequest request);

}
