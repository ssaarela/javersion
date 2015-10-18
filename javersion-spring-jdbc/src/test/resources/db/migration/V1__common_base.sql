create table VERSION_TYPE (
  NAME varchar(8),
  primary key (NAME)
);
insert into VERSION_TYPE values ('NORMAL');
insert into VERSION_TYPE values ('RESET');

create table REPOSITORY (
  ID varchar(32) not null,
  primary key (ID)
);
