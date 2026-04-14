-- 租户表新增租户类型字段
alter table if exists sys_tenant
    add column if not exists tenant_type varchar(32) default 'platform_operation';

comment on column sys_tenant.tenant_type is '租户类型';

update sys_tenant
set tenant_type = 'platform_operation'
where tenant_type is null or tenant_type = '';

-- 新增租户类型字典
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
select 16, '000000', '租户类型', 'sys_tenant_type', 103, 1, now(), null, null, '租户类型列表'
where not exists (
    select 1 from sys_dict_type where tenant_id = '000000' and dict_type = 'sys_tenant_type'
);

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark)
select 55, '000000', 1, '平台运营', 'platform_operation', 'sys_tenant_type', '', 'primary', 'Y', 103, 1, now(), null, null, '平台运营'
where not exists (
    select 1 from sys_dict_data where tenant_id = '000000' and dict_type = 'sys_tenant_type' and dict_value = 'platform_operation'
);

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark)
select 56, '000000', 2, '平台监管', 'platform_regulation', 'sys_tenant_type', '', 'warning', 'N', 103, 1, now(), null, null, '平台监管'
where not exists (
    select 1 from sys_dict_data where tenant_id = '000000' and dict_type = 'sys_tenant_type' and dict_value = 'platform_regulation'
);

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark)
select 57, '000000', 3, '云租户', 'cloud_tenant', 'sys_tenant_type', '', 'success', 'N', 103, 1, now(), null, null, '云租户'
where not exists (
    select 1 from sys_dict_data where tenant_id = '000000' and dict_type = 'sys_tenant_type' and dict_value = 'cloud_tenant'
);

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark)
select 58, '000000', 4, '代维服务商', 'service_provider', 'sys_tenant_type', '', 'info', 'N', 103, 1, now(), null, null, '代维服务商'
where not exists (
    select 1 from sys_dict_data where tenant_id = '000000' and dict_type = 'sys_tenant_type' and dict_value = 'service_provider'
);
