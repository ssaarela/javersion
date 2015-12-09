----------------------------------------------
-- For custom repositories, replace ENTITY_ --
----------------------------------------------

insert into REPOSITORY (ID) values ('ENTITY');

create table ENTITY (
  ID varchar(255) not null,

  primary key (ID)
);

create table ENTITY_VERSION (
  DOC_ID varchar(255) not null,
  REVISION varchar(32) not null,
  LOCAL_ORDINAL bigint not null,
  ORDINAL bigint,

  BRANCH varchar(128) not null,
  TYPE varchar(8) not null,

  primary key (REVISION),

  constraint ENTITY_VERSION_DOC_ID_FK
    foreign key (DOC_ID)
    references ENTITY (ID),

  constraint ENTITY_VERSION_TYPE_FK
    foreign key (TYPE)
    references VERSION_TYPE (NAME),

  constraint ENTITY_VERSION_ORDINAL_U
    unique (ORDINAL)
);

-- Load document and fetch updates since
create index ENTITY_VERSION_DOC_ID_IDX on ENTITY_VERSION (DOC_ID, LOCAL_ORDINAL, REVISION);
-- Fetch updates since
create index ENTITY_VERSION_REVISION_IDX on ENTITY_VERSION (REVISION, LOCAL_ORDINAL, DOC_ID);


create table ENTITY_VERSION_PARENT (
  REVISION varchar(32) not null,
  PARENT_REVISION varchar(32) not null,

  primary key (REVISION, PARENT_REVISION),

  constraint ENTITY_VERSION_PARENT_REVISION_FK
    foreign key (REVISION)
    references ENTITY_VERSION (REVISION),

  constraint ENTITY_VERSION_PARENT_PARENT_REVISION_FK
    foreign key (PARENT_REVISION)
    references ENTITY_VERSION (REVISION)
);


create table ENTITY_VERSION_PROPERTY (
  REVISION varchar(32) not null,

  PATH varchar(512) not null,
  -- n=null, O=object, A=array, s=string,
  -- b=boolean, l=long, d=double, D=bigdecimal
  TYPE char(1),
  STR varchar(1024),
  NBR bigint,

  primary key (REVISION, PATH),

  constraint ENTITY_VERSION_PROPERTY_REVISION_FK
    foreign key (REVISION)
    references ENTITY_VERSION (REVISION)
);
