create table version_type (
  name varchar(8),
  primary key (name)
);
insert into version_type values ('NORMAL');
insert into version_type values ('ROOT');
insert into version_type values ('REWRITE');

create table version (
  doc_id varchar(255) not null,
  revision_seq bigint not null,
  revision_node bigint not null,
  ordinal bigint not null,
  tx varchar(32),

  branch varchar(128) not null,
  type varchar(8) not null,

  primary key (revision_seq, revision_node),

  constraint version_type_fk foreign key (type) references version_type (name)
);

create table version_parent (
  child_revision_seq bigint not null,
  child_revision_node bigint not null,
  parent_revision_seq bigint not null,
  parent_revision_node bigint not null,

  primary key (child_revision_seq, child_revision_node, parent_revision_seq, parent_revision_node),

  constraint version_parent_child_revision_fk
    foreign key (child_revision_seq, child_revision_node)
    references version (revision_seq, revision_node),

  constraint version_parent_parent_revision_fk
    foreign key (parent_revision_seq, parent_revision_node)
    references version (revision_seq, revision_node)
);

create table version_property (
  revision_seq bigint not null,
  revision_node bigint not null,

  path varchar(512) not null,
  -- n=null, O=object, A=array, s=string,
  -- b=boolean, l=long, d=double, D=bigdecimal
  type char(1),
  str varchar(1024),
  nbr bigint,

  primary key (revision_seq, revision_node, path),

  constraint version_property_revision_fk
    foreign key (revision_seq, revision_node) references version (revision_seq, revision_node)
);

create table repository (
  -- node, ordinal
  key varchar(32) not null,
  val bigint
);
