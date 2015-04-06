create table version_type (
  name varchar(8),
  primary key (name)
);
insert into version_type values ('NORMAL');
insert into version_type values ('ROOT');
insert into version_type values ('REWRITE');

create table version (
  doc_id varchar(255) not null,
  revision varchar(32) not null,
  ordinal bigint not null,
  tx varchar(32),

  branch varchar(128) not null,
  type varchar(8) not null,

  primary key (revision),

  constraint version_type_fk foreign key (type) references version_type (name)
);

create sequence version_ordinal_seq start with 1 increment by 1 no cycle;
create index version_tx_idx on version (tx);
create index version_ordinal_idx on version (ordinal);


create table version_parent (
  child_revision varchar(32) not null,
  parent_revision varchar(32) not null,

  primary key (child_revision, parent_revision),

  constraint version_parent_child_revision_fk
    foreign key (child_revision)
    references version (revision),

  constraint version_parent_parent_revision_fk
    foreign key (parent_revision)
    references version (revision)
);

create table version_property (
  revision varchar(32) not null,

  path varchar(512) not null,
  -- n=null, O=object, A=array, s=string,
  -- b=boolean, l=long, d=double, D=bigdecimal
  type char(1),
  str varchar(1024),
  nbr bigint,

  primary key (revision, path),

  constraint version_property_revision_fk
    foreign key (revision) references version (revision)
);

create table repository (
  -- NODE, ORDINAL
  key varchar(32) not null,
  val bigint
);
