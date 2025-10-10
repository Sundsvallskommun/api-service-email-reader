
    create table attachment (
        created_at datetime(6),
        id bigint not null auto_increment,
        content_type varchar(255),
        email_id varchar(255),
        name varchar(255),
        content longblob,
        primary key (id)
    ) engine=InnoDB;

    create table credentials (
        enabled bit,
        created_at datetime(6),
        action varchar(255),
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
        html_message longtext,
        message longtext,
        primary key (id)
    ) engine=InnoDB;

    create table email_header (
        email_id varchar(255),
        id varchar(255) not null,
        header_key enum ('AUTO_SUBMITTED','IN_REPLY_TO','MESSAGE_ID','REFERENCES'),
        primary key (id)
    ) engine=InnoDB;

    create table email_header_value (
        order_index integer not null,
        value varchar(2048),
        header_id varchar(255) not null,
        primary key (order_index, header_id)
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

    create table graph_credentials (
        enabled bit,
        created_at datetime(6),
        client_id varchar(255),
        client_secret varchar(255),
        destination_folder varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        namespace varchar(255),
        tenant_id varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table graph_credentials_email_address (
        email_address varchar(255),
        graph_credentials_id varchar(255) not null
    ) engine=InnoDB;

    create table graph_credentials_metadata (
        graph_credentials_id varchar(255) not null,
        metadata varchar(255),
        metadata_key varchar(255) not null,
        primary key (graph_credentials_id, metadata_key)
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

    alter table if exists email_header 
       add constraint fk_email_header_email_id 
       foreign key (email_id) 
       references email (id);

    alter table if exists email_header_value 
       add constraint fk_header_value_header_id 
       foreign key (header_id) 
       references email_header (id);

    alter table if exists email_metadata 
       add constraint fk_email_metadata_email_id 
       foreign key (email_id) 
       references email (id);

    alter table if exists email_recipient 
       add constraint fk_email_recipient_email_id 
       foreign key (email_id) 
       references email (id);

    alter table if exists graph_credentials_email_address 
       add constraint fk_graph_credentials_email_address_graph_credentials_id 
       foreign key (graph_credentials_id) 
       references graph_credentials (id);

    alter table if exists graph_credentials_metadata 
       add constraint fk_graph_credentials_metadata_graph_credentials_id 
       foreign key (graph_credentials_id) 
       references graph_credentials (id);
