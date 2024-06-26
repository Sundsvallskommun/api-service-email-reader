package se.sundsvall.emailreader.utility;

import java.io.Serial;

public class EncryptionException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 5212638561309961670L;

	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(final String message) {
		super(message);
	}

}
