create table VERSION_TYPE (
  NAME varchar(8),
  primary key (NAME)
);
insert into VERSION_TYPE values ('NORMAL');
insert into VERSION_TYPE values ('RESET');

--------------------------------------------
-- For custom repositories, replace TEST_ --
--------------------------------------------

create table TEST_REPOSITORY (
  ID varchar(32) not null,
  ORDINAL bigint not null
);
insert into TEST_REPOSITORY (ID, ORDINAL) values ('repository', 0);

create table TEST_VERSION (
  DOC_ID varchar(255) not null,
  REVISION varchar(32) not null,
  ORDINAL bigint,
  TX_ORDINAL bigint,

  BRANCH varchar(128) not null,
  TYPE varchar(8) not null,

  primary key (REVISION),

  constraint TEST_VERSION_TYPE_FK foreign key (TYPE) references VERSION_TYPE (name)
);

create sequence TEST_VERSION_ORDINAL_SEQ start with 1 increment by 1 no cycle;

-- findDocuments(sinceOrdinal)
create index TEST_VERSION_ORDINAL_IDX on TEST_VERSION (ORDINAL, DOC_ID, REVISION);
-- findUncommittedRevisions
create index TEST_VERSION_TX_ORDINAL_IDX on TEST_VERSION (TX_ORDINAL, REVISION, DOC_ID);
-- getVersionsAndParents and getPropertiesByDocId
create index TEST_VERSION_DOC_ID_IDX on TEST_VERSION (DOC_ID, ORDINAL, REVISION);


create table TEST_VERSION_PARENT (
  REVISION varchar(32) not null,
  PARENT_REVISION varchar(32) not null,

  primary key (REVISION, PARENT_REVISION),

  constraint TEST_VERSION_PARENT_REVISION_FK
    foreign key (REVISION)
    references TEST_VERSION (REVISION),

  constraint TEST_VERSION_PARENT_PARENT_REVISION_FK
    foreign key (PARENT_REVISION)
    references TEST_VERSION (REVISION)
);


create table TEST_VERSION_PROPERTY (
  DOC_ID varchar(255) not null,
  REVISION varchar(32) not null,

  PATH varchar(512) not null,
  -- n=null, O=object, A=array, s=string,
  -- b=boolean, l=long, d=double, D=bigdecimal
  TYPE char(1),
  STR varchar(1024),
  NBR bigint,

  primary key (REVISION, PATH),

  constraint TEST_VERSION_PROPERTY_REVISION_FK
    foreign key (REVISION) references TEST_VERSION (REVISION)
);

create index TEST_VERSION_PROPERTY_DOC_ID_IDX on TEST_VERSION_PROPERTY (DOC_ID);
