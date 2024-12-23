package se.sundsvall.emailreader.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {

}
