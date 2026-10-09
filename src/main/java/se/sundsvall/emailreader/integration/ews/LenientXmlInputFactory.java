package se.sundsvall.emailreader.integration.ews;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import javax.xml.stream.XMLInputFactory;

/**
 * StAX input factory that accepts XML 1.1 style character references (e.g. {@code &#x3;}) in XML 1.0 documents.
 * <p>
 * Exchange encodes control characters found in message content (subject, body, headers) as character references, which
 * are only legal in XML 1.1. A strict parser rejects the whole EWS response, so the message can never be loaded and is
 * retried on every run. ews-java-api creates its parser through {@link XMLInputFactory#newInstance()} without any
 * configuration hook, so this factory is installed through the JAXP system property instead, at startup before the
 * application context is created so that every StAX consumer in the service gets the same factory.
 */
public class LenientXmlInputFactory extends WstxInputFactory {

	public LenientXmlInputFactory() {
		setProperty(WstxInputProperties.P_ALLOW_XML11_ESCAPED_CHARS_IN_XML10, true);
	}

	public static void install() {
		System.setProperty(XMLInputFactory.class.getName(), LenientXmlInputFactory.class.getName());
	}
}
