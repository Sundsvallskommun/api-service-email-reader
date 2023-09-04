-- Create the 'attachment' table
CREATE TABLE `attachment`
(
    `created_at`   datetime(6)  DEFAULT NULL,
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `content_type` varchar(255) DEFAULT NULL,
    `name`         varchar(255) DEFAULT NULL,
    `content`      longtext     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Create the 'credentials' table
CREATE TABLE `credentials`
(
    `created_at`         datetime(6)  DEFAULT NULL,
    `destination_folder` varchar(255) DEFAULT NULL,
    `domain`             varchar(255) DEFAULT NULL,
    `id`                 varchar(255) NOT NULL,
    `municipality_id`    varchar(255) DEFAULT NULL,
    `namespace`          varchar(255) DEFAULT NULL,
    `password`           varchar(255) DEFAULT NULL,
    `username`           varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Create the 'credentials_entity_email_address' table
CREATE TABLE `credentials_entity_email_address`
(
    `credentials_entity_id` varchar(255) NOT NULL,
    `email_address`         varchar(255) DEFAULT NULL,
    KEY `fk_credentials_entity` (`credentials_entity_id`),
    CONSTRAINT `fk_credentials_entity` FOREIGN KEY (`credentials_entity_id`) REFERENCES `credentials` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Create the 'email' table
CREATE TABLE `email`
(
    `created_at`      datetime(6)  DEFAULT NULL,
    `email_from`      varchar(255) DEFAULT NULL,
    `id`              varchar(255) NOT NULL,
    `municipality_id` varchar(255) DEFAULT NULL,
    `namespace`       varchar(255) DEFAULT NULL,
    `subject`         varchar(255) DEFAULT NULL,
    `message`         longtext     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Create the 'email_attachments' table
CREATE TABLE `email_attachments`
(
    `attachments_id`  bigint(20)   NOT NULL,
    `email_entity_id` varchar(255) NOT NULL,
    UNIQUE KEY `uk_attachment` (`attachments_id`),
    KEY `fk_email_entity` (`email_entity_id`),
    CONSTRAINT `fk_attachment` FOREIGN KEY (`attachments_id`) REFERENCES `attachment` (`id`),
    CONSTRAINT `fk_email_entity` FOREIGN KEY (`email_entity_id`) REFERENCES `email` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- Create the 'email_entity_to' table
CREATE TABLE `email_entity_to`
(
    `email_entity_id` varchar(255) NOT NULL,
    `email_to`        varchar(255) DEFAULT NULL,
    KEY `fk_email_entity_to` (`email_entity_id`),
    CONSTRAINT `fk_email_entity_to` FOREIGN KEY (`email_entity_id`) REFERENCES `email` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
