
    create table attachment (
        created_at datetime(6),
        id bigint not null auto_increment,
        content_type varchar(255),
        email_id varchar(255),
        name varchar(255),
        content longtext,
        primary key (id)
    ) engine=InnoDB;

    create table credentials (
        created_at datetime(6),
        destination_folder varchar(255),
        domain varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        namespace varchar(255),
        password varchar(255),
        username varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table credentials_email_address (
        credentials_id varchar(255) not null,
        email_address varchar(255)
    ) engine=InnoDB;

    create table credentials_metadata (
        credentials_id varchar(255) not null,
        metadata varchar(255),
        metadata_key varchar(255) not null,
        primary key (credentials_id, metadata_key)
    ) engine=InnoDB;

    create table email (
        created_at datetime(6),
        received_at datetime(6),
        id varchar(255) not null,
        municipality_id varchar(255),
        namespace varchar(255),
        sender varchar(255),
        subject varchar(255),
        message longtext,
        primary key (id)
    ) engine=InnoDB;

    create table email_metadata (
        email_id varchar(255) not null,
        metadata varchar(255),
        metadata_key varchar(255) not null,
        primary key (email_id, metadata_key)
    ) engine=InnoDB;

    create table email_recipient (
        email_id varchar(255) not null,
        recipients varchar(255)
    ) engine=InnoDB;

    alter table if exists attachment 
       add constraint fk_email_attachment_email_id 
       foreign key (email_id) 
       references email (id);

    alter table if exists credentials_email_address 
       add constraint fk_credentials_email_address_credentials_id 
       foreign key (credentials_id) 
       references credentials (id);

    alter table if exists credentials_metadata 
       add constraint fk_credentials_metadata_credentials_id 
       foreign key (credentials_id) 
       references credentials (id);

    alter table if exists email_metadata 
       add constraint fk_email_metadata_email_id 
       foreign key (email_id) 
       references email (id);

    alter table if exists email_recipient 
       add constraint fk_email_recipient_email_id 
       foreign key (email_id) 
       references email (id);
