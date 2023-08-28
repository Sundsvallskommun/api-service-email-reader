package se.sundsvall.emailreader.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.emailreader.integration.db.entity.EmailEntity;

public interface EmailRepository extends JpaRepository<EmailEntity, String> {

    List<EmailEntity> findByMunicipalityIdAndNamespace(String municipalityId, String namespace);

}
