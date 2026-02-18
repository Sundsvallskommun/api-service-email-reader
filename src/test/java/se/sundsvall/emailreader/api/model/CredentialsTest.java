package se.sundsvall.emailreader.api.model;

import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialsTest {

	@Test
	void testBuilderMethods() {

		final var result = Credentials.builder()
			.withDestinationFolder("someDestinationFolder")
			.withDomain("someDomain")
			.withNamespace("someNamespace")
			.withEmailAddress(Collections.singletonList("someEmailAddress"))
			.withUsername("someUsername")
			.withMetadata(Collections.singletonMap("someKey", "someValue"))
			.withPassword("somePassword")
			.withEnabled(true)
			.withId("someId").build();

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo("someId");
		assertThat(result.destinationFolder()).isEqualTo("someDestinationFolder");
		assertThat(result.domain()).isEqualTo("someDomain");
		assertThat(result.namespace()).isEqualTo("someNamespace");
		assertThat(result.username()).isEqualTo("someUsername");
		assertThat(result.password()).isEqualTo("somePassword");
		assertThat(result.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.emailAddress()).hasSize(1).element(0).satisfies(emailAddress -> assertThat(emailAddress).isEqualTo("someEmailAddress"));
		assertThat(result.enabled()).isTrue();

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(GraphCredentials.builder().build()).hasAllNullFieldsOrPropertiesExcept("enabled").satisfies(
			bean -> assertThat(bean.enabled()).isFalse());
	}

}
