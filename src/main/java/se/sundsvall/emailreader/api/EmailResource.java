package se.sundsvall.emailreader.api;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import jakarta.websocket.server.PathParam;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@Validated
@RequestMapping(path = "/email",
    produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
)
@ApiResponse(
    responseCode = "400",
    description = "Bad Request",
    content = @Content(schema = @Schema(implementation = Problem.class))
)
@ApiResponse(
    responseCode = "500",
    description = "Internal Server Error",
    content = @Content(schema = @Schema(implementation = Problem.class))
)
@ApiResponse(
    responseCode = "502",
    description = "Bad Gateway",
    content = @Content(schema = @Schema(implementation = Problem.class))
)
public class EmailResource {

    private final EmailService service;

    private final EWSIntegration integration;

    public EmailResource(final EmailService service, final EWSIntegration integration) {
        this.service = service;
        this.integration = integration;
    }

    @Operation(
        summary = "Get a list of emails",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Ok",
                useReturnTypeSchema = true
            )

        }
    )
    @GetMapping()
    public ResponseEntity<List<Email>> getAllEmails(@PathParam("municipalityId") final String municipalityId, @PathParam("namespace") final String namespace) {
        return ResponseEntity.ok(service.getAllEmails(municipalityId, namespace));
    }

    @Operation(
        summary = "Delete an email by messageID",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "No content"
            )
        }
    )
    @DeleteMapping(path = "{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteEmail(
        @Parameter(name = "id", description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @PathVariable("id") final String id) {
        service.deleteEmail(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<List<Email>> test() {
        return ResponseEntity.ok(integration.pageThroughEntireInbox("archive"));
    }

}
