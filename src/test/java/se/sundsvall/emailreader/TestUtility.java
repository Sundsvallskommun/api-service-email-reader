package se.sundsvall.emailreader;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

public class TestUtility {


    public static Credentials createCredentials() {
        return createCredentialsWithPassword(null);

    }

    public static Credentials createCredentialsWithPassword(final String password) {
        return Credentials.builder()
            .withDestinationFolder("someDestinationFolder")
            .withDomain("someDomain")
            .withNamespace("someNamespace")
            .withMunicipalityId("someMunicipalityId")
            .withUsername("someUsername")
            .withId("someId")
            .withPassword(password)
            .build();
    }

    public static CredentialsEntity createCredentialsEntity() {

        return CredentialsEntity.builder()
            .withDestinationFolder("someDestinationFolder")
            .withDomain("someDomain")
            .withNamespace("someNamespace")
            .withMunicipalityId("someMunicipalityId")
            .withUsername("someUsername")
            .withId("someId")
            .withPassword("somePassword")
            .build();
    }

}
