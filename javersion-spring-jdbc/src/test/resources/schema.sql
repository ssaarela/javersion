create table VERSION_TYPE (
  NAME varchar(8),
  primary key (NAME)
);
insert into VERSION_TYPE values ('NORMAL');
insert into VERSION_TYPE values ('RESET');

------------------------------------------------
-- For custom repositories, replace DOCUMENT_ --
------------------------------------------------

create table DOCUMENT_REPOSITORY (
  ID varchar(32) not null,
  ORDINAL bigint not null
);
insert into DOCUMENT_REPOSITORY (ID, ORDINAL) values ('repository', 0);

create table DOCUMENT_VERSION (
  DOC_ID varchar(255) not null,
  REVISION varchar(32) not null,
  ORDINAL bigint,
  LOCAL_ORDINAL bigint,

  BRANCH varchar(128) not null,
  TYPE varchar(8) not null,

  primary key (REVISION),

  constraint DOCUMENT_VERSION_TYPE_FK foreign key (TYPE) references VERSION_TYPE (name)
);

create sequence DOCUMENT_VERSION_ORDINAL_SEQ start with 1 increment by 1 no cycle;

-- Find un published versions
create index DOCUMENT_VERSION_LOCAL_ORDINAL_IDX on DOCUMENT_VERSION (LOCAL_ORDINAL, REVISION, DOC_ID);
-- Load document and fetch updates since
create index DOCUMENT_VERSION_DOC_ID_IDX on DOCUMENT_VERSION (DOC_ID, ORDINAL, REVISION);
-- Fetch updates since
create index DOCUMENT_VERSION_REVISION_IDX on DOCUMENT_VERSION (REVISION, ORDINAL, DOC_ID);

create table DOCUMENT_VERSION_PARENT (
  REVISION varchar(32) not null,
  PARENT_REVISION varchar(32) not null,

  primary key (REVISION, PARENT_REVISION),

  constraint DOCUMENT_VERSION_PARENT_REVISION_FK
    foreign key (REVISION)
    references DOCUMENT_VERSION (REVISION),

  constraint DOCUMENT_VERSION_PARENT_PARENT_REVISION_FK
    foreign key (PARENT_REVISION)
    references DOCUMENT_VERSION (REVISION)
);


create table DOCUMENT_VERSION_PROPERTY (
  REVISION varchar(32) not null,

  PATH varchar(512) not null,
  -- n=null, O=object, A=array, s=string,
  -- b=boolean, l=long, d=double, D=bigdecimal
  TYPE char(1),
  STR varchar(1024),
  NBR bigint,

  primary key (REVISION, PATH),

  constraint DOCUMENT_VERSION_PROPERTY_REVISION_FK
    foreign key (REVISION)
    references DOCUMENT_VERSION (REVISION)
);
