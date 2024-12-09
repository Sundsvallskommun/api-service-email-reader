package se.sundsvall.emailreader.integration.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.messaging")
@Getter
@Setter
public class MessagingProperties {

	private String recipientAdress;

	/// BASE PROPERTIES
	private String baseUrl;

	private int readTimeout = 15;

	private int connectTimeout = 5;

	private String tokenUri;

	private String clientId;

	private String clientSecret;

	private String grantType = "client_credentials";

}
