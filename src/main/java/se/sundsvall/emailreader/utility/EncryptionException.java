package se.sundsvall.emailreader.utility;

public class EncryptionException extends RuntimeException {

	private static final long serialVersionUID = 5212638561309961670L;

	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(final String message) {
		super(message);
	}
}
