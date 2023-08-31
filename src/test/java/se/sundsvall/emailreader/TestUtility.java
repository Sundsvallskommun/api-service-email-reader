package se.sundsvall.emailreader;

import java.util.Collections;
import java.util.List;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;

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
            .withEmailAdress(Collections.singletonList("someEmailAdress"))
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
            .withEmailAdress(Collections.singletonList("someEmailAdress"))
            .withId("someId")
            .withPassword("somePassword")
            .build();
    }

    public static Email createEmail() {

        return Email.builder()
            .withId("someId")
            .withSubject("someSubject")
            .withTo(List.of("someTo"))
            .withFrom("someFrom")
            .withMessage("someMessage")
            .withAttachments(List.of(Email.Attachment.builder()
                .withName("someName")
                .withContent("someContent")
                .withContentType("someContentType")
                .build()))
            .build();
    }

    public static EmailEntity createEmailEntity() {

        return EmailEntity.builder()
            .withSubject("someSubject")
            .withTo(List.of("someTo"))
            .withFrom("someFrom").withMessage("someMessage")
            .withId("someId")
            .withNamespace("someNamespace")
            .withMunicipalityId("someMunicipalityId")
            .withAttachments(List.of(
                AttachmentEntity.builder()
                    .withName("someName")
                    .withContent("someContent")
                    .withContentType("someContentType")
                    .build()
            ))
            .build();
    }

}
