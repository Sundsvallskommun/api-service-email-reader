package se.sundsvall.emailreader.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@Tag(name = "Email", description = "Email")
@RequestMapping(path = "/{municipalityId}/email",
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
class EmailResource {

	private final EmailService service;

	EmailResource(final EmailService service) {
		this.service = service;
	}

	@Operation(description = "Get a list of emails")
	@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	@GetMapping("/{namespace}")
	ResponseEntity<List<Email>> getAllEmails(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "A specific namespace", example = "CONTACTCENTER")
		@PathVariable("namespace") final String namespace) {
		return ResponseEntity.ok(service.getAllEmails(municipalityId, namespace));
	}

	@Operation(description = "Delete an email by id")
	@ApiResponse(responseCode = "204", description = "No content")
	@DeleteMapping(path = "/{id}")
	ResponseEntity<Void> deleteEmail(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "id", description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1")
		@PathVariable("id") @ValidUuid final String id) {
		service.deleteEmail(municipalityId, id);
		return ResponseEntity.noContent().build();
	}

}
