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

create sequence agent_package_seq;



