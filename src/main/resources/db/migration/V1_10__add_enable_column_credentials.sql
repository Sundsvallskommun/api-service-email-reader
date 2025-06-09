alter table credentials
    add column enabled bit;

alter table graph_credentials
    add column enabled bit;
