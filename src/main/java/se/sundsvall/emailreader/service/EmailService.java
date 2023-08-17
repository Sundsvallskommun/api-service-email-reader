package se.sundsvall.emailreader.service;


import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Email;

@Service
public class EmailService {

    public List<Email> getAllEmails(final String municipalityId, final String namespace) {
        return List.of(Email.builder()
            .withSubject("someSubject")
            .withTo("someTo")
            .withFrom("someFrom")
            .withMessage("someMessage")
            .withMessageID("someMessageId")
            .build());
    }

    public void deleteEmail(final String messageID) {
        // TODO integrate with JPA to delete message by messageID;
    }
}
