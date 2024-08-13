package se.sundsvall.emailreader.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class CredentialsTest {

	@Test
	void testBuilderAndGetters() {

		final var result = Credentials.builder()
			.withDestinationFolder("someDestinationFolder")
			.withDomain("someDomain")
			.withNamespace("someNamespace")
			.withEmailAddress(Collections.singletonList("someEmailAddress"))
			.withUsername("someUsername")
			.withMetadata(Collections.singletonMap("someKey", "someValue"))
			.withPassword("somePassword")
			.withId("someId").build();

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo("someId");
		assertThat(result.destinationFolder()).isEqualTo("someDestinationFolder");
		assertThat(result.domain()).isEqualTo("someDomain");
		assertThat(result.namespace()).isEqualTo("someNamespace");
		assertThat(result.username()).isEqualTo("someUsername");
		assertThat(result.password()).isEqualTo("somePassword");
		assertThat(result.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.emailAddress()).hasSize(1).element(0).satisfies(emailAddress ->
			assertThat(emailAddress).isEqualTo("someEmailAddress"));

	}

	@Test
	void testSetters() {

		var result = Credentials.builder().build();

		result = result.withDestinationFolder("someDestinationFolder")
			.withDomain("someDomain")
			.withNamespace("someNamespace")
			.withUsername("someUsername")
			.withPassword("somePassword")
			.withEmailAddress(Collections.singletonList("someEmailAddress"))
			.withMetadata(Collections.singletonMap("someKey", "someValue"))
			.withId("someId");

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo("someId");
		assertThat(result.destinationFolder()).isEqualTo("someDestinationFolder");
		assertThat(result.domain()).isEqualTo("someDomain");
		assertThat(result.namespace()).isEqualTo("someNamespace");
		assertThat(result.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.username()).isEqualTo("someUsername");
		assertThat(result.password()).isEqualTo("somePassword");
		assertThat(result.emailAddress()).hasSize(1).element(0).satisfies(emailAddress ->
			assertThat(emailAddress).isEqualTo("someEmailAddress"));
	}

}
