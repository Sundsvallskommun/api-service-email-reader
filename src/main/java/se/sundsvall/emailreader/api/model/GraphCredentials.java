package se.sundsvall.emailreader.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
@Schema(name = "GraphCredentials", description = "Email credentials to use for authentication against a Microsoft Graph server.")
public record GraphCredentials(

	@Schema(description = "Credentials id", accessMode = Schema.AccessMode.READ_ONLY, example = "74531aac-ffea-42cc-8a0a-52715bb27850") String id,

	@NotBlank @Schema(description = "Client secret to use for authentication against the Microsoft Graph server. Do note it should be the secret value and not the Secret ID.",
		example = "mySecretClientSecret",
		accessMode = WRITE_ONLY,
		requiredMode = REQUIRED) String clientSecret,

	@NotBlank @Schema(description = "Tenant ID to use for authentication against the Microsoft Graph server.", example = "myTenantId", accessMode = WRITE_ONLY, requiredMode = REQUIRED) String tenantId,

	@NotBlank @Schema(description = "Client ID to use for authentication against the Microsoft Graph server.", example = "myClientId", accessMode = WRITE_ONLY, requiredMode = REQUIRED) String clientId,

	@NotEmpty @ArraySchema(arraySchema = @Schema(
		implementation = String.class,
		description = "List of email addresses to check for new emails. The provided graph application must have access to these email addresses.",
		example = "[\"myothersupportemail@sundsvall.se\", \"mysupportemail@sundsvall.se\"]",
		requiredMode = REQUIRED)) List<String> emailAddress,

	@NotEmpty @Schema(description = "Namespace", example = "my.namespace", requiredMode = REQUIRED) String namespace,

	@Schema(description = "Metadata to add to the email.", example = "{\"casetype\":\"myCaseType\", \"key2\":\"value2\"}", requiredMode = REQUIRED) Map<String, String> metadata,

	@NotBlank @Schema(description = "The folder to move emails to after processing.", example = "Processed", requiredMode = REQUIRED) String destinationFolder) {

}
