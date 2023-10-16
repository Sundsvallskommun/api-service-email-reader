
    create table attachment (
        created_at datetime(6),
        id bigint not null auto_increment,
        content_type varchar(255),
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

    create table credentials_metadata (
        credentials_id varchar(255) not null,
        metadata varchar(255),
        metadata_key varchar(255) not null,
        primary key (credentials_id, metadata_key)
    ) engine=InnoDB;

    create table credentials_entity_email_address (
        credentials_entity_id varchar(255) not null,
        email_address varchar(255)
    ) engine=InnoDB;

    create table email (
        created_at datetime(6),
        email_from varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        namespace varchar(255),
        subject varchar(255),
        message longtext,
        primary key (id)
    ) engine=InnoDB;

    create table email_attachments (
        attachments_id bigint not null,
        email_entity_id varchar(255) not null
    ) engine=InnoDB;

    create table email_metadata (
        email_id varchar(255) not null,
        metadata varchar(255),
        metadata_key varchar(255) not null,
        primary key (email_id, metadata_key)
    ) engine=InnoDB;

    create table email_entity_to (
        email_entity_id varchar(255) not null,
        email_to varchar(255)
    ) engine=InnoDB;

    alter table if exists email_attachments 
       add constraint UK_qvr8yk266hyp5874ig8boji6g unique (attachments_id);

    alter table if exists credentials_metadata 
       add constraint fk_credentials_metadata_credentials_id 
       foreign key (credentials_id) 
       references credentials (id);

    alter table if exists credentials_entity_email_address 
       add constraint FKeywhaa4sq2pax54uf0ivfutes 
       foreign key (credentials_entity_id) 
       references credentials (id);

    alter table if exists email_attachments 
       add constraint FKbr86rsl9c7uodnbcn8rxy8akk 
       foreign key (attachments_id) 
       references attachment (id);

    alter table if exists email_attachments 
       add constraint FKgnj1j7sycs8037dpx9i62v3l6 
       foreign key (email_entity_id) 
       references email (id);

    alter table if exists email_metadata 
       add constraint fk_email_metadata_email_id 
       foreign key (email_id) 
       references email (id);

    alter table if exists email_entity_to 
       add constraint FKep0rcp33s79wcn2r9vjaiaa8g 
       foreign key (email_entity_id) 
       references email (id);
