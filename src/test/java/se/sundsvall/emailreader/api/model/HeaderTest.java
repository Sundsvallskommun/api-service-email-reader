package se.sundsvall.emailreader.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.MESSAGE_ID;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;

import org.junit.jupiter.api.Test;

class HeaderTest {

	@Test
	void enums() {
		assertThat(Header.values()).containsExactlyInAnyOrder(IN_REPLY_TO, REFERENCES, MESSAGE_ID, AUTO_SUBMITTED);
	}

	@Test
	void enumValues() {
		assertThat(IN_REPLY_TO.getName()).isEqualTo("In-Reply-To");
		assertThat(REFERENCES.getName()).isEqualTo("References");
		assertThat(MESSAGE_ID.getName()).isEqualTo("Message-ID");
		assertThat(AUTO_SUBMITTED.getName()).isEqualTo("Auto-Submitted");
	}

}
