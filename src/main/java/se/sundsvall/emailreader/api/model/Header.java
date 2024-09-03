package se.sundsvall.emailreader.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(enumAsRef = true)
public enum Header {
	IN_REPLY_TO("In-Reply-To"),
	REFERENCES("References"),
	MESSAGE_ID("Message-ID"),
	AUTO_SUBMITTED("Auto-Submitted");

	private final String name;

	Header(final String name) {
		this.name = name;
	}
}
