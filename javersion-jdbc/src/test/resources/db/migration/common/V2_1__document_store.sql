------------------------------------------------
-- For custom repositories, replace DOCUMENT_ --
------------------------------------------------

insert into REPOSITORY (ID) values ('DOCUMENT');

create table DOCUMENT_VERSION (
  DOC_ID varchar(255) not null,
  REVISION varchar(32) not null,
  -- 0 = squashed, 1 = normal/active
  STATUS numeric(1) not null,
  TX_ORDINAL bigint,
  ORDINAL bigint,

  BRANCH varchar(128) not null,
  TYPE varchar(8) not null,

  primary key (REVISION),

  constraint DOCUMENT_VERSION_TYPE_FK
    foreign key (TYPE)
    references VERSION_TYPE (NAME),

  constraint DOCUMENT_VERSION_ORDINAL_U
    unique (ORDINAL),

  constraint DOCUMENT_VERSION_STATE_CHK
    check (STATUS in (0, 1))
);

create sequence DOCUMENT_VERSION_ORDINAL_SEQ start with 1 increment by 1 no cycle;

-- Find unpublished versions
create index DOCUMENT_VERSION_TX_ORDINAL_IDX on DOCUMENT_VERSION (TX_ORDINAL, REVISION, DOC_ID);
-- Load document and fetch updates since
create index DOCUMENT_VERSION_DOC_ID_IDX on DOCUMENT_VERSION (DOC_ID, STATUS, ORDINAL, REVISION);
-- Fetch updates since
create index DOCUMENT_VERSION_REVISION_IDX on DOCUMENT_VERSION (REVISION, ORDINAL, DOC_ID);


create table DOCUMENT_VERSION_PARENT (
  REVISION varchar(32) not null,
  PARENT_REVISION varchar(32) not null,
  -- 0 = squashed, 1 = normal/active, 2 = redundant
  STATUS numeric(1) not null,

  primary key (REVISION, PARENT_REVISION),

  constraint DOCUMENT_VERSION_PARENT_REVISION_FK
    foreign key (REVISION)
    references DOCUMENT_VERSION (REVISION),

  constraint DOCUMENT_VERSION_PARENT_PARENT_REVISION_FK
    foreign key (PARENT_REVISION)
    references DOCUMENT_VERSION (REVISION),

  constraint DOCUMENT_VERSION_PARENT_STATE_CHK
    check (STATUS in (0, 1, 2))
);


create table DOCUMENT_VERSION_PROPERTY (
  REVISION varchar(32) not null,
  -- 0 = squashed, 1 = normal/active, 2 = redundant
  STATUS numeric(1) not null,

  PATH varchar(512) not null,
  -- n=null, O=object, A=array, s=string,
  -- b=boolean, l=long, d=double, D=bigdecimal
  TYPE char(1) not null,
  STR varchar(1024),
  NBR bigint,

  primary key (REVISION, PATH),

  constraint DOCUMENT_VERSION_PROPERTY_REVISION_FK
    foreign key (REVISION)
    references DOCUMENT_VERSION (REVISION),

  constraint DOCUMENT_VERSION_PROPERTY_STATE_CHK
    check (STATUS in (0, 1, 2))
);
