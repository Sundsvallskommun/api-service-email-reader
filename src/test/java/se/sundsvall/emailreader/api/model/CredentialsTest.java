package se.sundsvall.emailreader.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CredentialsTest {

    @Test
    void testBuilderAndGetters() {

        final var result = Credentials.builder()
            .withDestinationFolder("someDestinationFolder")
            .withDomain("someDomain")
            .withNamespace("someNamespace")
            .withMunicipalityId("someMunicipalityId")
            .withUsername("someUsername")
            .withPassword("somePassword")
            .withId("someId").build();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("someId");
        assertThat(result.destinationFolder()).isEqualTo("someDestinationFolder");
        assertThat(result.domain()).isEqualTo("someDomain");
        assertThat(result.namespace()).isEqualTo("someNamespace");
        assertThat(result.municipalityId()).isEqualTo("someMunicipalityId");
        assertThat(result.username()).isEqualTo("someUsername");
        assertThat(result.password()).isEqualTo("somePassword");

    }

    @Test
    void testSetters() {
        
        var result = Credentials.builder().build();

        result = result.withDestinationFolder("someDestinationFolder")
            .withDomain("someDomain")
            .withNamespace("someNamespace")
            .withMunicipalityId("someMunicipalityId")
            .withUsername("someUsername")
            .withPassword("somePassword")
            .withId("someId");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("someId");
        assertThat(result.destinationFolder()).isEqualTo("someDestinationFolder");
        assertThat(result.domain()).isEqualTo("someDomain");
        assertThat(result.namespace()).isEqualTo("someNamespace");
        assertThat(result.municipalityId()).isEqualTo("someMunicipalityId");
        assertThat(result.username()).isEqualTo("someUsername");
        assertThat(result.password()).isEqualTo("somePassword");
    }

}