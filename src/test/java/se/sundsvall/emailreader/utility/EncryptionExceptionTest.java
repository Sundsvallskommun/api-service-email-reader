package se.sundsvall.emailreader.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EncryptionExceptionTest {

	@Test
	void testConstructor() {

		final var message = "someMessage";

		assertThat(new EncryptionException("someMessage")).hasMessage(message);
	}

	@Test
	void testConstructorWithCause() {

		final var message = "someMessage";
		final var cause = new RuntimeException();

		assertThat(new EncryptionException("someMessage", new RuntimeException())).hasMessage(message).hasCause(cause);
	}
}
