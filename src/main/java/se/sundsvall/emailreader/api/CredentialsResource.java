package se.sundsvall.emailreader.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.service.CredentialsService;

@RestController
@Validated
@Tag(name = "Credentials", description = "Credentials")
@RequestMapping(path = "/{municipalityId}/credentials")
@ApiResponse(
	responseCode = "400",
	description = "Bad request",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	})))
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(
	responseCode = "502",
	description = "Bad Gateway",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CredentialsResource {

	private final CredentialsService credentialsService;

	CredentialsResource(final CredentialsService credentialsService) {
		this.credentialsService = credentialsService;
	}

	@Operation(description = "Get a list of credentials", responses = {
		@ApiResponse(responseCode = "200", description = "Ok", useReturnTypeSchema = true)
	})
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<Credentials>> getAllByMunicipalityId(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId) {

		return ok(credentialsService.getCredentialsByMunicipalityId(municipalityId));
	}

	@Operation(description = "Create credentials", responses = {
		@ApiResponse(responseCode = "204", description = "No content", useReturnTypeSchema = true)
	})
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> create(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Valid @RequestBody final Credentials credentials) {

		credentialsService.create(municipalityId, credentials);

		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Update credentials", responses = {
		@ApiResponse(responseCode = "204", description = "No content")
	})
	@PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> update(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "id", description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1") @PathVariable @ValidUuid final String id,
		@Valid @RequestBody final Credentials credentials) {

		credentialsService.update(municipalityId, id, credentials);

		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Delete credentials by id", responses = {
		@ApiResponse(responseCode = "204", description = "No content", useReturnTypeSchema = true)
	})
	@DeleteMapping(path = "/{id}", produces = ALL_VALUE)
	ResponseEntity<Void> delete(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "id", description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1") @PathVariable @ValidUuid final String id) {

		credentialsService.delete(municipalityId, id);

		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
