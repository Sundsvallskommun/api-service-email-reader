create table graph_credentials
(
    created_at         datetime(6),
    client_id          varchar(255),
    client_secret      varchar(255),
    destination_folder varchar(255),
    id                 varchar(255) not null,
    municipality_id    varchar(255),
    namespace          varchar(255),
    tenant_id          varchar(255),
    primary key (id)
) engine = InnoDB;

create table graph_credentials_email_address
(
    email_address        varchar(255),
    graph_credentials_id varchar(255) not null
) engine = InnoDB;

create table graph_credentials_metadata
(
    graph_credentials_id varchar(255) not null,
    metadata             varchar(255),
    metadata_key         varchar(255) not null,
    primary key (graph_credentials_id, metadata_key)
) engine = InnoDB;


alter table if exists graph_credentials_email_address
    add constraint fk_graph_credentials_email_address_graph_credentials_id
        foreign key (graph_credentials_id)
            references graph_credentials (id);

alter table if exists graph_credentials_metadata
    add constraint fk_graph_credentials_metadata_graph_credentials_id
        foreign key (graph_credentials_id)
            references graph_credentials (id);
