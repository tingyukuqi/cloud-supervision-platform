-- ----------------------------
-- 供应链模块基础表 Oracle 脚本
-- 说明：
-- 1. JSON/扩展字段统一使用 CLOB 存储
-- 2. verify_ssl 使用 NUMBER(1)，1 表示校验 SSL，0 表示不校验
-- 3. Oracle 版本兼容考虑，索引名统一控制在较短长度
-- 回滚顺序：
-- drop table supply_org_cloud_tenant_bind;
-- drop table supply_cloud_tenant;
-- drop table supply_event_log;
-- drop table supply_collect_log;
-- drop table supply_event_subscription;
-- drop table supply_collect_config;
-- drop table supply_cloud_platform;
-- drop table supply_physical_resource;
-- drop table supply_supplier_user;
-- drop table supply_supplier_platform_account;
-- drop table supply_supplier;
-- ----------------------------

create table supply_supplier (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    supplier_code varchar2(64) not null,
    supplier_name varchar2(64) not null,
    supplier_short_name varchar2(64) default null,
    supplier_type varchar2(32) not null,
    credit_code varchar2(32) default null,
    service_scope varchar2(255) default null,
    contact_name varchar2(64) default null,
    contact_phone varchar2(32) default null,
    contact_email varchar2(128) default null,
    address varchar2(255) default null,
    cooperation_type varchar2(32) default null,
    status char(1) default '0' not null,
    onboard_time timestamp(6) default null,
    offboard_time timestamp(6) default null,
    remark varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_supplier add constraint pk_supply_supplier primary key (id);

comment on table supply_supplier is '供应商主数据';
comment on column supply_supplier.id is '主键ID';
comment on column supply_supplier.tenant_id is '租户编号';
comment on column supply_supplier.supplier_code is '供应商编码';
comment on column supply_supplier.supplier_name is '供应商名称';
comment on column supply_supplier.supplier_short_name is '供应商简称';
comment on column supply_supplier.supplier_type is '供应商类型';
comment on column supply_supplier.credit_code is '统一社会信用代码';
comment on column supply_supplier.service_scope is '服务范围';
comment on column supply_supplier.contact_name is '联系人';
comment on column supply_supplier.contact_phone is '联系电话';
comment on column supply_supplier.contact_email is '联系邮箱';
comment on column supply_supplier.address is '联系地址';
comment on column supply_supplier.cooperation_type is '合作方式';
comment on column supply_supplier.status is '状态（0启用 1停用）';
comment on column supply_supplier.onboard_time is '合作开始时间';
comment on column supply_supplier.offboard_time is '合作结束时间';
comment on column supply_supplier.remark is '备注';
comment on column supply_supplier.create_dept is '创建部门';
comment on column supply_supplier.create_by is '创建人';
comment on column supply_supplier.create_time is '创建时间';
comment on column supply_supplier.update_by is '更新人';
comment on column supply_supplier.update_time is '更新时间';
comment on column supply_supplier.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_sup_code on supply_supplier (tenant_id, supplier_code, del_flag);
create unique index uk_sup_sup_name on supply_supplier (tenant_id, supplier_name, del_flag);
create index idx_sup_sup_status on supply_supplier (tenant_id, status);

create table supply_supplier_platform_account (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    supplier_id number(20) not null,
    cloud_platform_id number(20) default null,
    account_name varchar2(128) not null,
    account_type varchar2(32) default null,
    account_identifier varchar2(128) not null,
    account_status char(1) default '0' not null,
    last_verified_time timestamp(6) default null,
    remark varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_supplier_platform_account add constraint pk_sup_sup_acc primary key (id);

comment on table supply_supplier_platform_account is '供应商平台账号';
comment on column supply_supplier_platform_account.id is '主键ID';
comment on column supply_supplier_platform_account.tenant_id is '租户编号';
comment on column supply_supplier_platform_account.supplier_id is '供应商ID';
comment on column supply_supplier_platform_account.cloud_platform_id is '云平台ID';
comment on column supply_supplier_platform_account.account_name is '账号名称';
comment on column supply_supplier_platform_account.account_type is '账号类型';
comment on column supply_supplier_platform_account.account_identifier is '账号标识';
comment on column supply_supplier_platform_account.account_status is '账号状态（0启用 1停用）';
comment on column supply_supplier_platform_account.last_verified_time is '最近校验时间';
comment on column supply_supplier_platform_account.remark is '备注';
comment on column supply_supplier_platform_account.create_dept is '创建部门';
comment on column supply_supplier_platform_account.create_by is '创建人';
comment on column supply_supplier_platform_account.create_time is '创建时间';
comment on column supply_supplier_platform_account.update_by is '更新人';
comment on column supply_supplier_platform_account.update_time is '更新时间';
comment on column supply_supplier_platform_account.del_flag is '删除标志（0存在 1删除）';

create index idx_sup_acc_sup on supply_supplier_platform_account (tenant_id, supplier_id);
create index idx_sup_acc_plat on supply_supplier_platform_account (tenant_id, cloud_platform_id);

create table supply_supplier_user (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    supplier_id number(20) not null,
    user_id number(20) not null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_supplier_user add constraint pk_supply_supplier_user primary key (id);

comment on table supply_supplier_user is '供应商用户绑定';
comment on column supply_supplier_user.id is '主键ID';
comment on column supply_supplier_user.tenant_id is '租户编号';
comment on column supply_supplier_user.supplier_id is '供应商ID';
comment on column supply_supplier_user.user_id is '系统用户ID';
comment on column supply_supplier_user.create_dept is '创建部门';
comment on column supply_supplier_user.create_by is '创建人';
comment on column supply_supplier_user.create_time is '创建时间';
comment on column supply_supplier_user.update_by is '更新人';
comment on column supply_supplier_user.update_time is '更新时间';
comment on column supply_supplier_user.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_user_bind on supply_supplier_user (tenant_id, user_id, del_flag);

create table supply_physical_resource (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    resource_code varchar2(64) not null,
    supplier_id number(20) not null,
    device_name varchar2(128) not null,
    device_type varchar2(64) not null,
    device_model varchar2(128) default null,
    serial_number varchar2(128) not null,
    asset_tag varchar2(64) default null,
    resource_status varchar2(32) not null,
    rack_location varchar2(128) default null,
    idc_location varchar2(128) default null,
    manage_ip varchar2(64) default null,
    purchase_date date default null,
    expire_date date default null,
    spec_payload clob default null,
    remark varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_physical_resource add constraint pk_sup_phy_res primary key (id);

comment on table supply_physical_resource is '物理资源台账';
comment on column supply_physical_resource.id is '主键ID';
comment on column supply_physical_resource.tenant_id is '租户编号';
comment on column supply_physical_resource.resource_code is '资源编号';
comment on column supply_physical_resource.supplier_id is '供应商ID';
comment on column supply_physical_resource.device_name is '设备名称';
comment on column supply_physical_resource.device_type is '设备类型';
comment on column supply_physical_resource.device_model is '设备型号';
comment on column supply_physical_resource.serial_number is '序列号';
comment on column supply_physical_resource.asset_tag is '资产标签';
comment on column supply_physical_resource.resource_status is '资源状态';
comment on column supply_physical_resource.rack_location is '机柜位置';
comment on column supply_physical_resource.idc_location is '机房位置';
comment on column supply_physical_resource.manage_ip is '管理IP';
comment on column supply_physical_resource.purchase_date is '采购日期';
comment on column supply_physical_resource.expire_date is '到期日期';
comment on column supply_physical_resource.spec_payload is '规格扩展JSON';
comment on column supply_physical_resource.remark is '备注';
comment on column supply_physical_resource.create_dept is '创建部门';
comment on column supply_physical_resource.create_by is '创建人';
comment on column supply_physical_resource.create_time is '创建时间';
comment on column supply_physical_resource.update_by is '更新人';
comment on column supply_physical_resource.update_time is '更新时间';
comment on column supply_physical_resource.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_res_code on supply_physical_resource (tenant_id, resource_code, del_flag);
create unique index uk_sup_res_sn on supply_physical_resource (tenant_id, serial_number, del_flag);
create index idx_sup_res_sup on supply_physical_resource (tenant_id, supplier_id);
create index idx_sup_res_type on supply_physical_resource (tenant_id, device_type);
create index idx_sup_res_loc on supply_physical_resource (tenant_id, idc_location);

create table supply_cloud_platform (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    platform_code varchar2(64) not null,
    platform_name varchar2(128) not null,
    platform_type varchar2(64) not null,
    provider_code varchar2(64) not null,
    resource_pool_code varchar2(64) default null,
    region_code varchar2(64) default null,
    access_url varchar2(255) not null,
    api_version varchar2(32) default null,
    description varchar2(500) default null,
    status char(1) default '0' not null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_cloud_platform add constraint pk_supply_cloud_platform primary key (id);

comment on table supply_cloud_platform is '云平台主数据';
comment on column supply_cloud_platform.id is '主键ID';
comment on column supply_cloud_platform.tenant_id is '租户编号';
comment on column supply_cloud_platform.platform_code is '平台编码';
comment on column supply_cloud_platform.platform_name is '平台名称';
comment on column supply_cloud_platform.platform_type is '平台类型';
comment on column supply_cloud_platform.provider_code is '供应商编码';
comment on column supply_cloud_platform.resource_pool_code is '资源池编码';
comment on column supply_cloud_platform.region_code is '区域编码';
comment on column supply_cloud_platform.access_url is '接入地址';
comment on column supply_cloud_platform.api_version is 'API版本';
comment on column supply_cloud_platform.description is '平台描述';
comment on column supply_cloud_platform.status is '状态（0启用 1停用）';
comment on column supply_cloud_platform.create_dept is '创建部门';
comment on column supply_cloud_platform.create_by is '创建人';
comment on column supply_cloud_platform.create_time is '创建时间';
comment on column supply_cloud_platform.update_by is '更新人';
comment on column supply_cloud_platform.update_time is '更新时间';
comment on column supply_cloud_platform.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_cp_code on supply_cloud_platform (tenant_id, platform_code, del_flag);
create unique index uk_sup_cp_name on supply_cloud_platform (tenant_id, platform_name, del_flag);
create index idx_sup_cp_type on supply_cloud_platform (tenant_id, platform_type);
create index idx_sup_cp_pool on supply_cloud_platform (tenant_id, resource_pool_code);

create table supply_collect_config (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    cloud_platform_id number(20) not null,
    provider_code varchar2(64) not null,
    collect_url varchar2(255) not null,
    sync_endpoint varchar2(255) default null,
    collect_scope varchar2(32) not null,
    collect_mode varchar2(32) not null,
    sync_strategy varchar2(32) not null,
    connector_code varchar2(64) not null,
    auth_type varchar2(32) not null,
    auth_payload clob default null,
    scope_filter clob default null,
    collect_options clob default null,
    execute_cycle varchar2(32) not null,
    timeout_seconds number(10) default 30 not null,
    retry_times number(10) default 0 not null,
    verify_ssl number(1) default 1 not null,
    status char(1) default '0' not null,
    last_collect_time timestamp(6) default null,
    last_success_time timestamp(6) default null,
    last_collect_status varchar2(32) default null,
    last_error_message varchar2(500) default null,
    next_collect_time timestamp(6) default null,
    remark varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_collect_config add constraint pk_supply_collect_config primary key (id);

comment on table supply_collect_config is '快照采集配置';
comment on column supply_collect_config.id is '主键ID';
comment on column supply_collect_config.tenant_id is '租户编号';
comment on column supply_collect_config.cloud_platform_id is '云平台ID';
comment on column supply_collect_config.provider_code is '供应商编码';
comment on column supply_collect_config.collect_url is '采集地址';
comment on column supply_collect_config.sync_endpoint is '同步回调地址';
comment on column supply_collect_config.collect_scope is '采集范围';
comment on column supply_collect_config.collect_mode is '采集模式';
comment on column supply_collect_config.sync_strategy is '同步策略';
comment on column supply_collect_config.connector_code is '连接器编码';
comment on column supply_collect_config.auth_type is '认证类型';
comment on column supply_collect_config.auth_payload is '认证载荷（加密存储）';
comment on column supply_collect_config.scope_filter is '采集范围过滤JSON';
comment on column supply_collect_config.collect_options is '采集选项JSON';
comment on column supply_collect_config.execute_cycle is '执行周期';
comment on column supply_collect_config.timeout_seconds is '超时秒数';
comment on column supply_collect_config.retry_times is '重试次数';
comment on column supply_collect_config.verify_ssl is '是否校验SSL证书（1校验 0不校验）';
comment on column supply_collect_config.status is '状态（0启用 1停用）';
comment on column supply_collect_config.last_collect_time is '最近采集时间';
comment on column supply_collect_config.last_success_time is '最近成功时间';
comment on column supply_collect_config.last_collect_status is '最近采集状态';
comment on column supply_collect_config.last_error_message is '最近错误信息';
comment on column supply_collect_config.next_collect_time is '下次采集时间';
comment on column supply_collect_config.remark is '备注';
comment on column supply_collect_config.create_dept is '创建部门';
comment on column supply_collect_config.create_by is '创建人';
comment on column supply_collect_config.create_time is '创建时间';
comment on column supply_collect_config.update_by is '更新人';
comment on column supply_collect_config.update_time is '更新时间';
comment on column supply_collect_config.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_cc_scope on supply_collect_config (tenant_id, cloud_platform_id, collect_scope, del_flag);
create index idx_sup_cc_status on supply_collect_config (tenant_id, status);

create table supply_event_subscription (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    cloud_platform_id number(20) not null,
    provider_code varchar2(64) not null,
    event_scope varchar2(32) not null,
    ingest_mode varchar2(32) not null,
    topic_name varchar2(128) default null,
    consumer_group varchar2(128) default null,
    endpoint_path varchar2(255) default null,
    auth_type varchar2(32) default null,
    auth_payload clob default null,
    data_format varchar2(32) not null,
    schema_version varchar2(32) not null,
    status char(1) default '0' not null,
    last_event_time timestamp(6) default null,
    last_error_message varchar2(500) default null,
    remark varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_event_subscription add constraint pk_sup_evt_sub primary key (id);

comment on table supply_event_subscription is '实时事件订阅配置';
comment on column supply_event_subscription.id is '主键ID';
comment on column supply_event_subscription.tenant_id is '租户编号';
comment on column supply_event_subscription.cloud_platform_id is '云平台ID';
comment on column supply_event_subscription.provider_code is '供应商编码';
comment on column supply_event_subscription.event_scope is '事件范围';
comment on column supply_event_subscription.ingest_mode is '接入模式';
comment on column supply_event_subscription.topic_name is '主题名称';
comment on column supply_event_subscription.consumer_group is '消费组';
comment on column supply_event_subscription.endpoint_path is 'Webhook接收路径';
comment on column supply_event_subscription.auth_type is '鉴权类型';
comment on column supply_event_subscription.auth_payload is '鉴权载荷（加密存储）';
comment on column supply_event_subscription.data_format is '数据格式';
comment on column supply_event_subscription.schema_version is 'Schema版本';
comment on column supply_event_subscription.status is '状态（0启用 1停用）';
comment on column supply_event_subscription.last_event_time is '最近事件时间';
comment on column supply_event_subscription.last_error_message is '最近错误信息';
comment on column supply_event_subscription.remark is '备注';
comment on column supply_event_subscription.create_dept is '创建部门';
comment on column supply_event_subscription.create_by is '创建人';
comment on column supply_event_subscription.create_time is '创建时间';
comment on column supply_event_subscription.update_by is '更新人';
comment on column supply_event_subscription.update_time is '更新时间';
comment on column supply_event_subscription.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_es_scope on supply_event_subscription (tenant_id, cloud_platform_id, event_scope, del_flag);
create index idx_sup_es_status on supply_event_subscription (tenant_id, status);

create table supply_collect_log (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    collect_config_id number(20) not null,
    cloud_platform_id number(20) not null,
    collect_scope varchar2(32) not null,
    execute_mode varchar2(32) not null,
    job_instance_id varchar2(128) default null,
    trace_id varchar2(128) default null,
    trigger_user_id number(20) default null,
    start_time timestamp(6) default null,
    end_time timestamp(6) default null,
    duration_ms number(20) default null,
    result_status varchar2(32) default null,
    sync_status varchar2(32) default null,
    resource_count number(10) default null,
    cloud_tenant_count number(10) default null,
    sync_record_count number(10) default null,
    config_snapshot clob default null,
    error_message varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_collect_log add constraint pk_supply_collect_log primary key (id);

comment on table supply_collect_log is '采集执行日志';
comment on column supply_collect_log.id is '主键ID';
comment on column supply_collect_log.tenant_id is '租户编号';
comment on column supply_collect_log.collect_config_id is '采集配置ID';
comment on column supply_collect_log.cloud_platform_id is '云平台ID';
comment on column supply_collect_log.collect_scope is '采集范围';
comment on column supply_collect_log.execute_mode is '执行模式';
comment on column supply_collect_log.job_instance_id is '任务实例ID';
comment on column supply_collect_log.trace_id is '链路追踪ID';
comment on column supply_collect_log.trigger_user_id is '触发用户ID';
comment on column supply_collect_log.start_time is '开始时间';
comment on column supply_collect_log.end_time is '结束时间';
comment on column supply_collect_log.duration_ms is '耗时毫秒';
comment on column supply_collect_log.result_status is '执行结果状态';
comment on column supply_collect_log.sync_status is '同步状态';
comment on column supply_collect_log.resource_count is '资源数量';
comment on column supply_collect_log.cloud_tenant_count is '云租户数量';
comment on column supply_collect_log.sync_record_count is '同步记录数量';
comment on column supply_collect_log.config_snapshot is '配置快照JSON';
comment on column supply_collect_log.error_message is '错误信息';
comment on column supply_collect_log.create_dept is '创建部门';
comment on column supply_collect_log.create_by is '创建人';
comment on column supply_collect_log.create_time is '创建时间';
comment on column supply_collect_log.update_by is '更新人';
comment on column supply_collect_log.update_time is '更新时间';
comment on column supply_collect_log.del_flag is '删除标志（0存在 1删除）';

create index idx_sup_cl_cfg on supply_collect_log (tenant_id, collect_config_id, start_time);

create table supply_event_log (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    subscription_id number(20) not null,
    cloud_platform_id number(20) not null,
    event_scope varchar2(32) not null,
    event_key varchar2(255) default null,
    source_event_id varchar2(128) default null,
    event_time timestamp(6) default null,
    ingest_time timestamp(6) default null,
    process_status varchar2(32) default null,
    trace_id varchar2(128) default null,
    raw_payload clob default null,
    normalized_payload clob default null,
    error_message varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_event_log add constraint pk_supply_event_log primary key (id);

comment on table supply_event_log is '实时事件日志';
comment on column supply_event_log.id is '主键ID';
comment on column supply_event_log.tenant_id is '租户编号';
comment on column supply_event_log.subscription_id is '订阅ID';
comment on column supply_event_log.cloud_platform_id is '云平台ID';
comment on column supply_event_log.event_scope is '事件范围';
comment on column supply_event_log.event_key is '事件业务键';
comment on column supply_event_log.source_event_id is '源事件ID';
comment on column supply_event_log.event_time is '事件时间';
comment on column supply_event_log.ingest_time is '接收时间';
comment on column supply_event_log.process_status is '处理状态';
comment on column supply_event_log.trace_id is '链路追踪ID';
comment on column supply_event_log.raw_payload is '原始事件JSON';
comment on column supply_event_log.normalized_payload is '标准化事件JSON';
comment on column supply_event_log.error_message is '错误信息';
comment on column supply_event_log.create_dept is '创建部门';
comment on column supply_event_log.create_by is '创建人';
comment on column supply_event_log.create_time is '创建时间';
comment on column supply_event_log.update_by is '更新人';
comment on column supply_event_log.update_time is '更新时间';
comment on column supply_event_log.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_evt_src on supply_event_log (tenant_id, subscription_id, source_event_id, del_flag);
create index idx_sup_evt_key on supply_event_log (tenant_id, event_scope, event_key);
create index idx_sup_evt_time on supply_event_log (tenant_id, event_time);

create table supply_cloud_tenant (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    cloud_platform_id number(20) not null,
    external_tenant_id varchar2(128) not null,
    external_parent_id varchar2(128) default null,
    cloud_tenant_name varchar2(128) not null,
    cloud_tenant_code varchar2(128) default null,
    tenant_status varchar2(32) default null,
    region_code varchar2(64) default null,
    source_account_identifier varchar2(128) default null,
    raw_payload clob default null,
    last_sync_time timestamp(6) default null,
    sync_status varchar2(32) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_cloud_tenant add constraint pk_supply_cloud_tenant primary key (id);

comment on table supply_cloud_tenant is '云租户快照';
comment on column supply_cloud_tenant.id is '主键ID';
comment on column supply_cloud_tenant.tenant_id is '租户编号';
comment on column supply_cloud_tenant.cloud_platform_id is '云平台ID';
comment on column supply_cloud_tenant.external_tenant_id is '外部租户ID';
comment on column supply_cloud_tenant.external_parent_id is '外部父租户ID';
comment on column supply_cloud_tenant.cloud_tenant_name is '云租户名称';
comment on column supply_cloud_tenant.cloud_tenant_code is '云租户编码';
comment on column supply_cloud_tenant.tenant_status is '租户状态';
comment on column supply_cloud_tenant.region_code is '区域编码';
comment on column supply_cloud_tenant.source_account_identifier is '来源账号标识';
comment on column supply_cloud_tenant.raw_payload is '原始数据JSON';
comment on column supply_cloud_tenant.last_sync_time is '最近同步时间';
comment on column supply_cloud_tenant.sync_status is '同步状态';
comment on column supply_cloud_tenant.create_dept is '创建部门';
comment on column supply_cloud_tenant.create_by is '创建人';
comment on column supply_cloud_tenant.create_time is '创建时间';
comment on column supply_cloud_tenant.update_by is '更新人';
comment on column supply_cloud_tenant.update_time is '更新时间';
comment on column supply_cloud_tenant.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_ct_ext on supply_cloud_tenant (tenant_id, cloud_platform_id, external_tenant_id, del_flag);
create index idx_sup_ct_plat on supply_cloud_tenant (tenant_id, cloud_platform_id);

create table supply_org_cloud_tenant_bind (
    id number(20) not null,
    tenant_id varchar2(20) not null,
    org_id number(20) not null,
    cloud_platform_id number(20) not null,
    cloud_tenant_snapshot_id number(20) not null,
    bind_status varchar2(32) not null,
    effective_time timestamp(6) default null,
    invalid_time timestamp(6) default null,
    binding_remark varchar2(500) default null,
    create_dept number(20) default null,
    create_by number(20) default null,
    create_time timestamp(6) default null,
    update_by number(20) default null,
    update_time timestamp(6) default null,
    del_flag char(1) default '0' not null
);

alter table supply_org_cloud_tenant_bind add constraint pk_sup_org_ct_bind primary key (id);

comment on table supply_org_cloud_tenant_bind is '组织与云租户绑定';
comment on column supply_org_cloud_tenant_bind.id is '主键ID';
comment on column supply_org_cloud_tenant_bind.tenant_id is '租户编号';
comment on column supply_org_cloud_tenant_bind.org_id is '组织ID';
comment on column supply_org_cloud_tenant_bind.cloud_platform_id is '云平台ID';
comment on column supply_org_cloud_tenant_bind.cloud_tenant_snapshot_id is '云租户快照ID';
comment on column supply_org_cloud_tenant_bind.bind_status is '绑定状态';
comment on column supply_org_cloud_tenant_bind.effective_time is '生效时间';
comment on column supply_org_cloud_tenant_bind.invalid_time is '失效时间';
comment on column supply_org_cloud_tenant_bind.binding_remark is '绑定备注';
comment on column supply_org_cloud_tenant_bind.create_dept is '创建部门';
comment on column supply_org_cloud_tenant_bind.create_by is '创建人';
comment on column supply_org_cloud_tenant_bind.create_time is '创建时间';
comment on column supply_org_cloud_tenant_bind.update_by is '更新人';
comment on column supply_org_cloud_tenant_bind.update_time is '更新时间';
comment on column supply_org_cloud_tenant_bind.del_flag is '删除标志（0存在 1删除）';

create unique index uk_sup_org_ct on supply_org_cloud_tenant_bind (tenant_id, cloud_tenant_snapshot_id, del_flag);
