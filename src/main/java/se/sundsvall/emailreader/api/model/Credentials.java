package se.sundsvall.emailreader.api.model;


import static io.swagger.v3.oas.annotations.media.Schema.AccessMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;


@With
@Builder(setterPrefix = "with")
@Schema(name = "Credentials", description = "Email credentials to use for authentication against the email server")
public record Credentials(

    @Schema(description = "Credentials id", accessMode = AccessMode.READ_ONLY)
    String id,

    @NotBlank
    @Schema(description = "username to use for authentication against the email server", example = "joe01doe")
    String username,

    @NotBlank
    @Schema(description = "password to use for authentication against the email server", example = "mySecretPassword", accessMode = AccessMode.WRITE_ONLY)
    String password,

    @NotBlank
    @Schema(description = "domain for the email server", example = "https://mail.example.com/EWS/Exchange.asmx")
    String domain,

    @ValidMunicipalityId
    @Schema(description = "Municipality id", example = "2281")
    String municipalityId,

    @NotEmpty
    @Schema(description = "Namespace", example = "my.namespace")
    String namespace,

    @NotBlank
    @Schema(description = "The folder to move emails to after processing")
    String destinationFolder
) {


}
