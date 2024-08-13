package se.sundsvall.emailreader.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.service.CredentialsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@Tag(name = "Credentials", description = "Credentials")
@RequestMapping(path = "/{municipalityId}/credentials",
	produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
@ApiResponse(
	responseCode = "400",
	description = "Bad Request",
	content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(
	responseCode = "502",
	description = "Bad Gateway",
	content = @Content(schema = @Schema(implementation = Problem.class)))
class CredentialsResource {

	private final CredentialsService credentialsService;

	CredentialsResource(final CredentialsService credentialsService) {
		this.credentialsService = credentialsService;
	}

	@Operation(description = "Get a list of credentials")
	@ApiResponse(responseCode = "200", description = "Ok", useReturnTypeSchema = true)
	@GetMapping
	ResponseEntity<List<Credentials>> getAllByMunicipalityId(
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId) {

		return ResponseEntity.ok(credentialsService.getCredentialsByMunicipalityId(municipalityId));
	}

	@Operation(description = "Create credentials")
	@ApiResponse(responseCode = "204", description = "No content")
	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> create(
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId,
		@Valid @RequestBody final Credentials credentials) {

		credentialsService.create(municipalityId, credentials);

		return ResponseEntity.noContent().build();
	}

	@Operation(description = "Update credentials")
	@ApiResponse(responseCode = "204", description = "No content")
	@PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> update(
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable("id") @Schema(description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid final String id,
		@Valid @RequestBody final Credentials credentials) {

		credentialsService.update(municipalityId, id, credentials);

		return ResponseEntity.noContent().build();
	}

	@Operation(description = "Delete credentials by id")
	@ApiResponse(responseCode = "204", description = "No content")
	@DeleteMapping(path = "/{id}")
	ResponseEntity<Void> delete(
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable("id") @Schema(description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid final String id) {

		credentialsService.delete(municipalityId, id);

		return ResponseEntity.noContent().build();
	}

}
