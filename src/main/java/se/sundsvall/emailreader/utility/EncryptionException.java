package se.sundsvall.emailreader.utility;

public class EncryptionException extends RuntimeException {

    public EncryptionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(final String message) {
        super(message);
    }

}
