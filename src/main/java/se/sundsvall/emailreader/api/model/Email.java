package se.sundsvall.emailreader.api.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder(setterPrefix = "with")
@Schema(name = "Email", description = "Email", accessMode = Schema.AccessMode.READ_ONLY)
public record Email(

	@ArraySchema(arraySchema = @Schema(
		implementation = String.class,
		description = "Recipient E-Mail addresses",
		example = "[\"myothersupportemail@sundsvall.se\", \"mysupportemail@sundsvall.se\"]"))
	List<String> recipients,

	@Schema(description = "Sender E-Mail address", example = "sender@sundsvall.se")
	String sender,

	@Schema(description = "E-mail subject", example = "E-mail subject")
	String subject,

	@Schema(description = "E-mail plain-text body", example = "E-mail body")
	String message,

	@Schema(description = "E-mail message id", example = "74531aac-ffea-42cc-8a0a-52715bb27850")
	String id,
	@Schema(description = "Metadata for the email", example = "{\"casetype\":\"myCaseType\", \"key2\":\"value2\"}")
	Map<String, String> metadata,

	@Schema(description = "The date and time the email was received", example = "2021-09-01T12:00:00+02:00")
	OffsetDateTime receivedAt,

	@Schema(description = "The email headers")
	Map<Header, List<String>> headers,

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
