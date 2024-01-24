package se.sundsvall.emailreader.service;


import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.service.mapper.EmailMapper;

@Service
public class EmailService {

	private final EmailRepository emailRepository;


	public EmailService(final EmailRepository emailRepository) {
		this.emailRepository = emailRepository;
	}

	public List<Email> getAllEmails(final String municipalityId, final String namespace) {

		final var result = emailRepository.findByMunicipalityIdAndNamespace(municipalityId, namespace);

		return EmailMapper.toEmails(result);
	}

	public void deleteEmail(final String id) {

		emailRepository.deleteById(id);

	}

}
