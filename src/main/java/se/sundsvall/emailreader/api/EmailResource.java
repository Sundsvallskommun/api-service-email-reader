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

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
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

    public EmailResource(final EmailService service) {this.service = service;}

    @Operation(
        summary = "Get a list of emails",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Ok",
                useReturnTypeSchema = gittrue
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
    @DeleteMapping(path = "{messageID}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteEmail(@PathVariable final String messageID) {
        service.deleteEmail(messageID);
        return ResponseEntity.noContent().build();
    }
}
