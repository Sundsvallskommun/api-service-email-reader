package se.sundsvall.emailreader.api.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(name = "Email", description = "Email")
public record Email(

    @ArraySchema(schema = @Schema(implementation = String.class, description = "Recipient E-Mail addresses"))
    List<String> to,

    @Schema(description = "Sender E-Mail address")
    String from,

    @Schema(description = "E-mail subject")
    String subject,

    @Schema(description = "E-mail plain-text body")
    String message,

    @Schema(description = "E-mail message id")
    String id,

    @ArraySchema(schema = @Schema(implementation = Attachment.class))
    List<Attachment> attachments) {

    @Builder(setterPrefix = "with")
    @Schema(name = "EmailAttachment", description = "Attachment")
    public record Attachment(

        @Schema(description = "The attachment filename", example = "test.txt")
        String name,

        @Schema(description = "The attachment content type", example = "text/plain")
        String contentType,

        @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK")
        String content) {

    }

}
