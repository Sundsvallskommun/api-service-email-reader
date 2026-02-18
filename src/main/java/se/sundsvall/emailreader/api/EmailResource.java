package se.sundsvall.emailreader.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.service.EmailService;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@Tag(name = "Email", description = "Email")
@RequestMapping(path = "/{municipalityId}/email")

@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class EmailResource {

	private final EmailService service;

	EmailResource(final EmailService service) {
		this.service = service;
	}

	@Operation(description = "Get a list of emails", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	})
	@GetMapping(path = "/{namespace}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<Email>> getAllEmails(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "A specific namespace", example = "CONTACTCENTER") @PathVariable final String namespace) {

		return ok(service.getAllEmails(municipalityId, namespace));
	}

	@GetMapping(value = "/attachments/{attachmentId}", produces = ALL_VALUE)
	@Operation(summary = "Get a messageAttachment", description = "Returns a messageAttachment as a stream for the specified attachmentId", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	void getAttachment(
		@Parameter(name = "municipalityId", description = "Municipality Id", example = "2281", required = true) @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "attachmentId", description = "MessageId to fetch attachment for", example = "123", required = true) @PathVariable final int attachmentId, final HttpServletResponse response) {

		service.getMessageAttachmentStreamed(attachmentId, response);
	}

	@Operation(description = "Delete an email by id", responses = {
		@ApiResponse(responseCode = "204", description = "No content", useReturnTypeSchema = true)
	})
	@DeleteMapping(path = "/{id}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteEmail(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "id", description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1") @PathVariable @ValidUuid final String id) {

		service.deleteEmail(municipalityId, id);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
