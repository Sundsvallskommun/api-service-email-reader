package se.sundsvall.emailreader.api;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping(path = "/email",
    consumes = APPLICATION_JSON_VALUE,
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
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Email.class)))
            )
        }
    )
    @GetMapping()
    public ResponseEntity<List<Email>> getAllEmails() {
        return ResponseEntity.ok(service.getAllEmails());
    }

    @Operation(
        summary = "Get a single email",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Ok",
                content = @Content(schema = @Schema(implementation = Email.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content(schema = @Schema(implementation = Problem.class))
            )
        }
    )
    @GetMapping("{messageID}")
    public ResponseEntity<Email> getEmail(@PathVariable final String messageID) {

        return ResponseEntity.ok(service.getEmail(messageID));
    }

    @Operation(
        summary = "Delete a email by messageID",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "No content"
            )
        }
    )
    @DeleteMapping("{messageID}")
    public ResponseEntity<Void> deleteEmail(@PathVariable final String messageID) {
        service.deleteEmail(messageID);
        return ResponseEntity.noContent().build();
    }
}
