package se.sundsvall.emailreader.integration.ews;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LenientXmlInputFactoryTest {

	private static final String XML_WITH_CONTROL_CHARACTER_REFERENCE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Subject>Hello&#x3;World</Subject>";

	private static String readText(final XMLInputFactory factory) throws XMLStreamException {
		final var reader = factory.createXMLStreamReader(new StringReader(XML_WITH_CONTROL_CHARACTER_REFERENCE));
		reader.nextTag();
		return reader.getElementText();
	}

	@Test
	void defaultFactoryRejectsControlCharacterReference() {
		assertThatThrownBy(() -> readText(new WstxInputFactory()))
			.isInstanceOf(XMLStreamException.class)
			.hasMessageContaining("Illegal character entity: expansion character (code 0x3)");
	}

	@Test
	void lenientFactoryAcceptsControlCharacterReference() throws XMLStreamException {
		assertThat(readText(new LenientXmlInputFactory())).isEqualTo("Hello\u0003World");
	}
}
