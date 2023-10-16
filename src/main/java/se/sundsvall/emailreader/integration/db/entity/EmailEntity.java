package se.sundsvall.emailreader.integration.db.entity;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

import org.hibernate.Length;
import org.hibernate.annotations.UuidGenerator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	@ElementCollection
	@Column(name = "email_to")
	private List<String> to;

	@Column(name = "email_from")
	private String from;

	@Column(name = "subject")
	private String subject;

	@Column(name = "message", length = Length.LONG32)
	private String message;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AttachmentEntity> attachments;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ElementCollection
	@CollectionTable(
		name = "email_metadata",
		joinColumns = @JoinColumn(name = "email_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_email_metadata_email_id")))
	private Map<String, String> metadata;

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

}
