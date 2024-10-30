package se.sundsvall.emailreader.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import se.sundsvall.dept44.common.validators.annotation.MemberOf;
import se.sundsvall.emailreader.integration.db.entity.enums.Action;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
@Schema(name = "Credentials", description = "Email credentials to use for authentication against the email server")
public record Credentials(

	@Schema(description = "Credentials id", accessMode = AccessMode.READ_ONLY, example = "74531aac-ffea-42cc-8a0a-52715bb27850") String id,

	@NotBlank @Schema(description = "username to use for authentication against the email server", example = "joe01doe") String username,

	@NotBlank @Schema(description = "password to use for authentication against the email server", example = "mySecretPassword", accessMode = AccessMode.WRITE_ONLY) String password,

	@NotEmpty @ArraySchema(arraySchema = @Schema(
		implementation = String.class,
		description = "List of email addresses to check for new emails. The provided user must have access to these email addresses.",
		example = "[\"myothersupportemail@sundsvall.se\", \"mysupportemail@sundsvall.se\"]")) List<String> emailAddress,

	@NotBlank @Schema(description = "domain for the email server", example = "https://mail.example.com/EWS/Exchange.asmx") String domain,

	@NotEmpty @Schema(description = "Namespace", example = "my.namespace") String namespace,

	@MemberOf(Action.class) @Schema(description = "Action to take for new emails", example = "PERSIST") String action,

	@Schema(description = "Metadata to add to the email", example = "{\"casetype\":\"myCaseType\", \"key2\":\"value2\"}") Map<String, String> metadata,

	@NotBlank @Schema(description = "The folder to move emails to after processing", example = "Processed") String destinationFolder) {

}
