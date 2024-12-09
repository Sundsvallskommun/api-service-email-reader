package se.sundsvall.emailreader.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ServiceUtilTest {

	private static final String PATH = "mimetype_files/";
	private static final String IMG_FILE_NAME = "image.jpg";
	private static final String DOC_FILE_NAME = "document.doc";
	private static final String DOCX_FILE_NAME = "document.docx";
	private static final String PDF_FILE_NAME = "document.pdf";
	private static final String TXT_FILE_NAME = "document.txt";

	@Test
	void detectMimeTypeThrowsException() throws IOException {
		assertThat(ServiceUtil.detectMimeType(null, null)).isEqualTo("application/octet-stream");

		final var inputStream = spy(new ByteArrayInputStream("data".getBytes()));
		doThrow(new IOException()).when(inputStream).read(any(byte[].class));
		assertThat(ServiceUtil.detectMimeTypeFromStream(null, inputStream)).isEqualTo("application/octet-stream");
	}

	@Test
	void detectMimeType() throws IOException {
		// IMAGE
		assertThat(ServiceUtil.detectMimeTypeFromStream(IMG_FILE_NAME, getStream(PATH + IMG_FILE_NAME))).isEqualTo("image/jpeg");
		assertThat(ServiceUtil.detectMimeType(IMG_FILE_NAME, getStream(PATH + IMG_FILE_NAME).readAllBytes())).isEqualTo("image/jpeg");
		assertThat(ServiceUtil.detectMimeTypeFromStream(null, getStream(PATH + IMG_FILE_NAME))).isEqualTo("image/jpeg");

		// DOC
		assertThat(ServiceUtil.detectMimeTypeFromStream(DOC_FILE_NAME, getStream(PATH + DOC_FILE_NAME))).isEqualTo("application/msword");
		assertThat(ServiceUtil.detectMimeType(DOC_FILE_NAME, getStream(PATH + DOC_FILE_NAME).readAllBytes())).isEqualTo("application/msword");
		assertThat(ServiceUtil.detectMimeTypeFromStream(null, getStream(PATH + DOC_FILE_NAME))).isEqualTo("application/x-tika-msoffice");

		// DOCX
		assertThat(ServiceUtil.detectMimeTypeFromStream(DOCX_FILE_NAME, getStream(PATH + DOCX_FILE_NAME))).isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		assertThat(ServiceUtil.detectMimeType(DOCX_FILE_NAME, getStream(PATH + DOCX_FILE_NAME).readAllBytes())).isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		assertThat(ServiceUtil.detectMimeTypeFromStream(null, getStream(PATH + DOCX_FILE_NAME))).isEqualTo("application/x-tika-ooxml");

		// PDF
		assertThat(ServiceUtil.detectMimeTypeFromStream(PDF_FILE_NAME, getStream(PATH + PDF_FILE_NAME))).isEqualTo("application/pdf");
		assertThat(ServiceUtil.detectMimeType(PDF_FILE_NAME, getStream(PATH + PDF_FILE_NAME).readAllBytes())).isEqualTo("application/pdf");
		assertThat(ServiceUtil.detectMimeTypeFromStream(null, getStream(PATH + PDF_FILE_NAME))).isEqualTo("application/pdf");

		// TEXT
		assertThat(ServiceUtil.detectMimeTypeFromStream(TXT_FILE_NAME, getStream(PATH + TXT_FILE_NAME))).isEqualTo("text/plain");
		assertThat(ServiceUtil.detectMimeType(TXT_FILE_NAME, getStream(PATH + TXT_FILE_NAME).readAllBytes())).isEqualTo("text/plain");
		assertThat(ServiceUtil.detectMimeTypeFromStream(null, getStream(PATH + TXT_FILE_NAME))).isEqualTo("text/plain");
	}

	private InputStream getStream(final String path) throws IOException {
		return new ClassPathResource(path).getInputStream();
	}
}
