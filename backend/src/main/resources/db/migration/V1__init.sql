create table clients (
    id bigserial primary key,
    name varchar(160) not null,
    phone varchar(40) not null unique,
    created_at timestamptz not null default now()
);

create table order_requests (
    id bigserial primary key,
    client_id bigint not null references clients(id),
    comment text,
    source varchar(40) not null default 'SITE',
    status varchar(40) not null default 'NEW',
    ip_address varchar(80),
    user_agent text,
    created_at timestamptz not null default now()
);

create index idx_order_requests_created_at on order_requests(created_at desc);
create index idx_order_requests_status on order_requests(status);
