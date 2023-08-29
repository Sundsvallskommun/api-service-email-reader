package se.sundsvall.emailreader.integration.db.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credentials")
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CredentialsEntity {

    @Id
    @UuidGenerator
    @Column(name = "id")
    private String id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "domain")
    private String domain;

    @Column(name = "municipality_id")
    private String municipalityId;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "destination_folder")
    private String destinationFolder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ExcludeFromJacocoGeneratedCoverageReport
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

}