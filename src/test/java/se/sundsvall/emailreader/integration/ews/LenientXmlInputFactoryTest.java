package se.sundsvall.emailreader.integration.ews;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import microsoft.exchange.webservices.data.core.EwsXmlReader;
import microsoft.exchange.webservices.data.core.enumeration.misc.XmlNamespace;
import microsoft.exchange.webservices.data.security.XmlNodeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LenientXmlInputFactoryTest {

	private static final String XML_WITH_CONTROL_CHARACTER_REFERENCE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Subject>Hello&#x3;World</Subject>";

	private static String readText(final XMLInputFactory factory) throws XMLStreamException {
		final var reader = factory.createXMLStreamReader(new StringReader(XML_WITH_CONTROL_CHARACTER_REFERENCE));
		reader.nextTag();
		return reader.getElementText();
	}

	@AfterEach
	void tearDown() {
		System.clearProperty(XMLInputFactory.class.getName());
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

	@Test
	void installMakesEwsResponsesWithControlCharacterReferencesParseable() throws Exception {
		final var response = """
			<?xml version="1.0" encoding="utf-8"?>
			<t:Subject xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types">Hello&#x3;World</t:Subject>
			""";

		LenientXmlInputFactory.install();

		final var reader = new EwsXmlReader(new ByteArrayInputStream(response.getBytes(UTF_8)));
		reader.read(new XmlNodeType(XmlNodeType.START_DOCUMENT));
		reader.readStartElement(XmlNamespace.Types, "Subject");

		assertThat(reader.readValue()).isEqualTo("Hello\u0003World");
	}
}
