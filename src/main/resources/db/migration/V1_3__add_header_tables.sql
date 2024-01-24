create table email_header
(
    email_id   varchar(255),
    id         varchar(255) not null,
    header_key enum ('IN_REPLY_TO','REFERENCES','MESSAGE_ID'),
    primary key (id)
) engine = InnoDB;


create table email_header_value
(
    header_id varchar(255) not null,
    value     varchar(255)
) engine = InnoDB;

alter table if exists email_header
    add constraint fk_email_header_email_id
        foreign key (email_id)
            references email (id);

alter table if exists email_header_value
    add constraint fk_header_value_header_id
        foreign key (header_id)
            references email_header (id);
