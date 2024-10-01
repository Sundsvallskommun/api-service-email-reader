package se.sundsvall.emailreader.integration.messaging;

import java.util.concurrent.TimeUnit;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import feign.Request;
import feign.codec.ErrorDecoder;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class MessagingConfiguration {

	public static final String CLIENT_ID = "messaging";

	private final MessagingProperties properties;

	public MessagingConfiguration(MessagingProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer customizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(errorDecoder())
			.withRequestOptions(requestOptions())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
			.composeCustomizersToOne();
	}

	Request.Options requestOptions() {
		return new Request.Options(
			properties.getConnectTimeout(), TimeUnit.MILLISECONDS,
			properties.getReadTimeout(), TimeUnit.MILLISECONDS,
			true);

	}

	ClientRegistration clientRegistration() {
		return ClientRegistration
			.withRegistrationId(CLIENT_ID)
			.tokenUri(properties.getTokenUri())
			.clientId(properties.getClientId())
			.clientSecret(properties.getClientSecret())
			.authorizationGrantType(new AuthorizationGrantType(properties.getGrantType()))
			.build();
	}

	ErrorDecoder errorDecoder() {
		return new ProblemErrorDecoder(CLIENT_ID);
	}
}
