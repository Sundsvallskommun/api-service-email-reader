package se.sundsvall.emailreader.utility;

import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtil.class);
	private static final String MIME_ERROR_MSG = "Exception when detecting mime type of file with filename '%s'";
	private static final Tika DETECTOR = new Tika();

	private ServiceUtil() {
		// Prevent instantiation
	}

	public static String detectMimeType(final String fileName, final byte[] byteArray) {
		try (final var stream = new ByteArrayInputStream(byteArray)) {
			return detectMimeTypeFromStream(fileName, stream);
		} catch (final Exception e) {
			LOGGER.warn(String.format(MIME_ERROR_MSG, fileName), e);
			return APPLICATION_OCTET_STREAM_VALUE; // Return mime type for arbitrary binary files
		}
	}

	public static String detectMimeTypeFromStream(final String fileName, final InputStream stream) {
		try {
			return DETECTOR.detect(stream, fileName);
		} catch (final Exception e) {
			LOGGER.warn(String.format(MIME_ERROR_MSG, fileName), e);
			return APPLICATION_OCTET_STREAM_VALUE; // Return mime type for arbitrary binary files
		}
	}
}
