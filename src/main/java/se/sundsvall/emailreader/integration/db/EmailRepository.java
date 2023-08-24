package se.sundsvall.emailreader.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<EmailEntity, String> {

    List<EmailEntity> findByMunicipalityIdAndNamespace(String municipalityId, String namespace);

}
