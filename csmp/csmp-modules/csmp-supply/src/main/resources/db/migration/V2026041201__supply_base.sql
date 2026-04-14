-- 供应链模块基础表
-- ROLLBACK: drop table if exists supply_org_cloud_tenant_bind, supply_cloud_tenant, supply_event_log, supply_collect_log, supply_event_subscription, supply_collect_config, supply_cloud_platform, supply_physical_resource, supply_supplier_user, supply_supplier_platform_account, supply_supplier;

create table if not exists supply_supplier (
    id bigint primary key,
    tenant_id varchar(20) not null,
    supplier_code varchar(64) not null,
    supplier_name varchar(64) not null,
    supplier_short_name varchar(64),
    supplier_type varchar(32) not null,
    credit_code varchar(32),
    service_scope varchar(255),
    contact_name varchar(64),
    contact_phone varchar(32),
    contact_email varchar(128),
    address varchar(255),
    cooperation_type varchar(32),
    status char(1) not null default '0',
    onboard_time timestamp,
    offboard_time timestamp,
    remark varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_supplier_code on supply_supplier (tenant_id, supplier_code, del_flag);
create unique index if not exists uk_supply_supplier_name on supply_supplier (tenant_id, supplier_name, del_flag);
create index if not exists idx_supply_supplier_status on supply_supplier (tenant_id, status);

create table if not exists supply_supplier_platform_account (
    id bigint primary key,
    tenant_id varchar(20) not null,
    supplier_id bigint not null,
    cloud_platform_id bigint,
    account_name varchar(128) not null,
    account_type varchar(32),
    account_identifier varchar(128) not null,
    account_status char(1) not null default '0',
    last_verified_time timestamp,
    remark varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create index if not exists idx_supply_supplier_account_supplier on supply_supplier_platform_account (tenant_id, supplier_id);
create index if not exists idx_supply_supplier_account_platform on supply_supplier_platform_account (tenant_id, cloud_platform_id);

create table if not exists supply_supplier_user (
    id bigint primary key,
    tenant_id varchar(20) not null,
    supplier_id bigint not null,
    user_id bigint not null,
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_supplier_user on supply_supplier_user (tenant_id, user_id, del_flag);

create table if not exists supply_physical_resource (
    id bigint primary key,
    tenant_id varchar(20) not null,
    resource_code varchar(64) not null,
    supplier_id bigint not null,
    device_name varchar(128) not null,
    device_type varchar(64) not null,
    device_model varchar(128),
    serial_number varchar(128) not null,
    asset_tag varchar(64),
    resource_status varchar(32) not null,
    rack_location varchar(128),
    idc_location varchar(128),
    manage_ip varchar(64),
    purchase_date date,
    expire_date date,
    spec_payload jsonb,
    remark varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_physical_resource_code on supply_physical_resource (tenant_id, resource_code, del_flag);
create unique index if not exists uk_supply_physical_resource_sn on supply_physical_resource (tenant_id, serial_number, del_flag);
create index if not exists idx_supply_physical_resource_supplier on supply_physical_resource (tenant_id, supplier_id);
create index if not exists idx_supply_physical_resource_type on supply_physical_resource (tenant_id, device_type);
create index if not exists idx_supply_physical_resource_location on supply_physical_resource (tenant_id, idc_location);

create table if not exists supply_cloud_platform (
    id bigint primary key,
    tenant_id varchar(20) not null,
    platform_code varchar(64) not null,
    platform_name varchar(128) not null,
    platform_type varchar(64) not null,
    provider_code varchar(64) not null,
    resource_pool_code varchar(64),
    region_code varchar(64),
    access_url varchar(255) not null,
    api_version varchar(32),
    description varchar(500),
    status char(1) not null default '0',
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_cloud_platform_code on supply_cloud_platform (tenant_id, platform_code, del_flag);
create unique index if not exists uk_supply_cloud_platform_name on supply_cloud_platform (tenant_id, platform_name, del_flag);
create index if not exists idx_supply_cloud_platform_type on supply_cloud_platform (tenant_id, platform_type);
create index if not exists idx_supply_cloud_platform_pool on supply_cloud_platform (tenant_id, resource_pool_code);

create table if not exists supply_collect_config (
    id bigint primary key,
    tenant_id varchar(20) not null,
    cloud_platform_id bigint not null,
    provider_code varchar(64) not null,
    collect_url varchar(255) not null,
    sync_endpoint varchar(255),
    collect_scope varchar(32) not null,
    collect_mode varchar(32) not null,
    sync_strategy varchar(32) not null,
    connector_code varchar(64) not null,
    auth_type varchar(32) not null,
    auth_payload text,
    scope_filter jsonb,
    collect_options jsonb,
    execute_cycle varchar(32) not null,
    timeout_seconds integer not null default 30,
    retry_times integer not null default 0,
    verify_ssl boolean not null default true,
    status char(1) not null default '0',
    last_collect_time timestamp,
    last_success_time timestamp,
    last_collect_status varchar(32),
    last_error_message varchar(500),
    next_collect_time timestamp,
    remark varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_collect_config_scope on supply_collect_config (tenant_id, cloud_platform_id, collect_scope, del_flag);
create index if not exists idx_supply_collect_config_status on supply_collect_config (tenant_id, status);

create table if not exists supply_event_subscription (
    id bigint primary key,
    tenant_id varchar(20) not null,
    cloud_platform_id bigint not null,
    provider_code varchar(64) not null,
    event_scope varchar(32) not null,
    ingest_mode varchar(32) not null,
    topic_name varchar(128),
    consumer_group varchar(128),
    endpoint_path varchar(255),
    auth_type varchar(32),
    auth_payload text,
    data_format varchar(32) not null,
    schema_version varchar(32) not null,
    status char(1) not null default '0',
    last_event_time timestamp,
    last_error_message varchar(500),
    remark varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_event_subscription_scope on supply_event_subscription (tenant_id, cloud_platform_id, event_scope, del_flag);
create index if not exists idx_supply_event_subscription_status on supply_event_subscription (tenant_id, status);

create table if not exists supply_collect_log (
    id bigint primary key,
    tenant_id varchar(20) not null,
    collect_config_id bigint not null,
    cloud_platform_id bigint not null,
    collect_scope varchar(32) not null,
    execute_mode varchar(32) not null,
    job_instance_id varchar(128),
    trace_id varchar(128),
    trigger_user_id bigint,
    start_time timestamp,
    end_time timestamp,
    duration_ms bigint,
    result_status varchar(32),
    sync_status varchar(32),
    resource_count integer,
    cloud_tenant_count integer,
    sync_record_count integer,
    config_snapshot jsonb,
    error_message varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create index if not exists idx_supply_collect_log_config on supply_collect_log (tenant_id, collect_config_id, start_time desc);

create table if not exists supply_event_log (
    id bigint primary key,
    tenant_id varchar(20) not null,
    subscription_id bigint not null,
    cloud_platform_id bigint not null,
    event_scope varchar(32) not null,
    event_key varchar(255),
    source_event_id varchar(128),
    event_time timestamp,
    ingest_time timestamp,
    process_status varchar(32),
    trace_id varchar(128),
    raw_payload jsonb,
    normalized_payload jsonb,
    error_message varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_event_source_id on supply_event_log (tenant_id, subscription_id, source_event_id, del_flag);
create index if not exists idx_supply_event_key on supply_event_log (tenant_id, event_scope, event_key);
create index if not exists idx_supply_event_time on supply_event_log (tenant_id, event_time desc);

create table if not exists supply_cloud_tenant (
    id bigint primary key,
    tenant_id varchar(20) not null,
    cloud_platform_id bigint not null,
    external_tenant_id varchar(128) not null,
    external_parent_id varchar(128),
    cloud_tenant_name varchar(128) not null,
    cloud_tenant_code varchar(128),
    tenant_status varchar(32),
    region_code varchar(64),
    source_account_identifier varchar(128),
    raw_payload jsonb,
    last_sync_time timestamp,
    sync_status varchar(32),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_cloud_tenant_external on supply_cloud_tenant (tenant_id, cloud_platform_id, external_tenant_id, del_flag);
create index if not exists idx_supply_cloud_tenant_platform on supply_cloud_tenant (tenant_id, cloud_platform_id);

create table if not exists supply_org_cloud_tenant_bind (
    id bigint primary key,
    tenant_id varchar(20) not null,
    org_id bigint not null,
    cloud_platform_id bigint not null,
    cloud_tenant_snapshot_id bigint not null,
    bind_status varchar(32) not null,
    effective_time timestamp,
    invalid_time timestamp,
    binding_remark varchar(500),
    create_dept bigint,
    create_by bigint,
    create_time timestamp,
    update_by bigint,
    update_time timestamp,
    del_flag char(1) not null default '0'
);

create unique index if not exists uk_supply_org_cloud_tenant on supply_org_cloud_tenant_bind (tenant_id, cloud_tenant_snapshot_id, del_flag);
