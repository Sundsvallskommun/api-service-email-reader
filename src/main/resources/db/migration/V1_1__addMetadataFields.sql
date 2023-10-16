create table if not exists credentials_metadata
(
    credentials_id varchar(255) not null,
    metadata       varchar(255),
    metadata_key   varchar(255) not null,
    primary key (credentials_id, metadata_key)
) engine = InnoDB;


create table if not exists email_metadata
(
    email_id     varchar(255) not null,
    metadata     varchar(255),
    metadata_key varchar(255) not null,
    primary key (email_id, metadata_key)
) engine = InnoDB;


alter table if exists credentials_metadata
    add constraint fk_credentials_metadata_credentials_id
        foreign key (credentials_id)
            references credentials (id);

alter table if exists email_metadata
    add constraint fk_email_metadata_email_id
        foreign key (email_id)
            references email (id);
