package se.sundsvall.emailreader.integration.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Length;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Entity
@Table(name = "email")
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Transient
	private String originalId;

	@ElementCollection
	@CollectionTable(
		name = "email_recipient",
		joinColumns = @JoinColumn(name = "email_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_email_recipient_email_id")))
	private List<String> recipients;

	@Column(name = "sender")
	private String sender;

	@Column(name = "subject")
	private String subject;

	@Column(name = "message", length = Length.LONG32)
	private String message;

	@Column(name = "html_message", length = Length.LONG32)
	private String htmlMessage;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "email_id",
		referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_email_attachment_email_id"))
	private List<AttachmentEntity> attachments;

	@Column(name = "received_at")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime receivedAt;

	@Column(name = "created_at")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime createdAt;

	@ElementCollection
	@CollectionTable(
		name = "email_metadata",
		joinColumns = @JoinColumn(name = "email_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_email_metadata_email_id")))
	private Map<String, String> metadata;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "email_id",
		referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_email_header_email_id"))
	private List<EmailHeaderEntity> headers;

	@PrePersist
	void prePersist() {
		createdAt = OffsetDateTime.now();
	}

}
