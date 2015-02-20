create table agent_package (
  id                        bigint not null,
  name                      varchar(255),
  version                   varchar(255),
  location                  varchar(255),
  status                    integer,
  updated_at                timestamp not null,
  constraint ck_agent_package_status check (status in (0,1,2,3)),
  constraint pk_agent_package primary key (id))
;

create table ccm_entity (
  id                        bigint not null,
  type                      integer,
  object_id                 varchar(255),
  address                   varchar(255),
  notification_count        bigint,
  status                    integer,
  updated_at                timestamp not null,
  constraint ck_ccm_entity_type check (type in (0,1,2,3)),
  constraint ck_ccm_entity_status check (status in (0,1)),
  constraint pk_ccm_entity primary key (id))
;

create sequence agent_package_seq;

create sequence ccm_entity_seq;



