alter table DOCUMENT_VERSION add column ID bigint;
alter table DOCUMENT_VERSION add column NAME varchar(255);

alter table ENTITY add column NAME varchar(255);
alter table ENTITY_VERSION add column COMMENT varchar(255);

create index ENTITY_VERSION_PROPERTY_STATUS_IDX on ENTITY_VERSION_PROPERTY (STATUS);
