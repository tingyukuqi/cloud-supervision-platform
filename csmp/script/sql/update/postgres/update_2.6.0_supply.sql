-- 供应链模块初始化（PostgreSQL）
-- 说明：
-- 1. 本脚本汇总供应链菜单、字典、角色三部分初始化内容
-- 2. 仅覆盖当前后端已实现的菜单与权限点
-- 3. 不预写未落地接口对应的菜单按钮，避免出现“有权限点、无功能”的假闭环
-- 4. 默认给超级管理员角色（role_id=1）授予新增菜单权限
-- ROLLBACK: DELETE FROM sys_role_menu WHERE role_id = '1' AND menu_id IN ('21000','21001','21002','21003','21004','21005','21010','21011','21012','21013','21014','21015','21016','21020','21021','21022','21023','21024','21025','21030','21031','21032','21033','21034','21035','21040','21041','21042','21043','21044','21050','21051','21060','21061','21062','21063','21064');
-- ROLLBACK: DELETE FROM sys_menu WHERE menu_id IN ('21000','21001','21002','21003','21004','21005','21010','21011','21012','21013','21014','21015','21016','21020','21021','21022','21023','21024','21025','21030','21031','21032','21033','21034','21035','21040','21041','21042','21043','21044','21050','21051','21060','21061','21062','21063','21064');

-- 供应链管理目录
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21000', '供应链管理', '0', '7', 'supply', '', '',
    1, 0, 'M', '0', '0', '', 'guide',
    103, 1, now(), NULL, NULL, '供应链管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21000');

-- 供应商管理
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21001', '供应商管理', '21000', '1', 'supplier', 'supply/supplier/index', '',
    1, 0, 'C', '0', '0', 'supply:supplier:list', 'peoples',
    103, 1, now(), NULL, NULL, '供应商管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21001');

INSERT INTO sys_menu SELECT '21002', '供应商查询', '21001', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:supplier:query', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21002');
INSERT INTO sys_menu SELECT '21003', '供应商新增', '21001', '2', '#', '', '', 1, 0, 'F', '0', '0', 'supply:supplier:add', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21003');
INSERT INTO sys_menu SELECT '21004', '供应商修改', '21001', '3', '#', '', '', 1, 0, 'F', '0', '0', 'supply:supplier:edit', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21004');
INSERT INTO sys_menu SELECT '21005', '供应商删除', '21001', '4', '#', '', '', 1, 0, 'F', '0', '0', 'supply:supplier:remove', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21005');

-- 物理资源管理
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21010', '物理资源管理', '21000', '2', 'physicalResource', 'supply/physicalResource/index', '',
    1, 0, 'C', '0', '0', 'supply:physicalResource:list', 'server',
    103, 1, now(), NULL, NULL, '物理资源管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21010');

INSERT INTO sys_menu SELECT '21011', '物理资源查询', '21010', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:physicalResource:query', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21011');
INSERT INTO sys_menu SELECT '21012', '物理资源新增', '21010', '2', '#', '', '', 1, 0, 'F', '0', '0', 'supply:physicalResource:add', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21012');
INSERT INTO sys_menu SELECT '21013', '物理资源修改', '21010', '3', '#', '', '', 1, 0, 'F', '0', '0', 'supply:physicalResource:edit', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21013');
INSERT INTO sys_menu SELECT '21014', '物理资源删除', '21010', '4', '#', '', '', 1, 0, 'F', '0', '0', 'supply:physicalResource:remove', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21014');
INSERT INTO sys_menu SELECT '21015', '物理资源导出', '21010', '5', '#', '', '', 1, 0, 'F', '0', '0', 'supply:physicalResource:export', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21015');
INSERT INTO sys_menu SELECT '21016', '物理资源导入', '21010', '6', '#', '', '', 1, 0, 'F', '0', '0', 'supply:physicalResource:import', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21016');

-- 云平台管理
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21020', '云平台管理', '21000', '3', 'cloudPlatform', 'supply/cloudPlatform/index', '',
    1, 0, 'C', '0', '0', 'supply:cloudPlatform:list', 'cloud',
    103, 1, now(), NULL, NULL, '云平台管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21020');

INSERT INTO sys_menu SELECT '21021', '云平台查询', '21020', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:cloudPlatform:query', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21021');
INSERT INTO sys_menu SELECT '21022', '云平台新增', '21020', '2', '#', '', '', 1, 0, 'F', '0', '0', 'supply:cloudPlatform:add', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21022');
INSERT INTO sys_menu SELECT '21023', '云平台修改', '21020', '3', '#', '', '', 1, 0, 'F', '0', '0', 'supply:cloudPlatform:edit', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21023');
INSERT INTO sys_menu SELECT '21024', '云平台删除', '21020', '4', '#', '', '', 1, 0, 'F', '0', '0', 'supply:cloudPlatform:remove', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21024');
INSERT INTO sys_menu SELECT '21025', '云平台导出', '21020', '5', '#', '', '', 1, 0, 'F', '0', '0', 'supply:cloudPlatform:export', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21025');

-- 采集配置
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21030', '采集配置', '21000', '4', 'collectConfig', 'supply/collectConfig/index', '',
    1, 0, 'C', '0', '0', 'supply:collectConfig:list', 'job',
    103, 1, now(), NULL, NULL, '采集配置菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21030');

INSERT INTO sys_menu SELECT '21031', '采集配置查询', '21030', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:collectConfig:query', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21031');
INSERT INTO sys_menu SELECT '21032', '采集配置新增', '21030', '2', '#', '', '', 1, 0, 'F', '0', '0', 'supply:collectConfig:add', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21032');
INSERT INTO sys_menu SELECT '21033', '采集配置修改', '21030', '3', '#', '', '', 1, 0, 'F', '0', '0', 'supply:collectConfig:edit', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21033');
INSERT INTO sys_menu SELECT '21034', '采集配置删除', '21030', '4', '#', '', '', 1, 0, 'F', '0', '0', 'supply:collectConfig:remove', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21034');
INSERT INTO sys_menu SELECT '21035', '采集配置执行', '21030', '5', '#', '', '', 1, 0, 'F', '0', '0', 'supply:collectConfig:execute', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21035');

-- 实时事件接入
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21040', '实时事件接入', '21000', '5', 'eventSubscription', 'supply/eventSubscription/index', '',
    1, 0, 'C', '0', '0', 'supply:eventSubscription:list', 'message',
    103, 1, now(), NULL, NULL, '实时事件接入菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21040');

INSERT INTO sys_menu SELECT '21041', '事件订阅查询', '21040', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:eventSubscription:query', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21041');
INSERT INTO sys_menu SELECT '21042', '事件订阅新增', '21040', '2', '#', '', '', 1, 0, 'F', '0', '0', 'supply:eventSubscription:add', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21042');
INSERT INTO sys_menu SELECT '21043', '事件订阅修改', '21040', '3', '#', '', '', 1, 0, 'F', '0', '0', 'supply:eventSubscription:edit', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21043');
INSERT INTO sys_menu SELECT '21044', '事件订阅删除', '21040', '4', '#', '', '', 1, 0, 'F', '0', '0', 'supply:eventSubscription:remove', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21044');

-- 云租户快照
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21050', '云租户快照', '21000', '6', 'cloudTenant', 'supply/cloudTenant/index', '',
    1, 0, 'C', '0', '0', 'supply:cloudTenant:list', 'international',
    103, 1, now(), NULL, NULL, '云租户快照菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21050');

INSERT INTO sys_menu SELECT '21051', '云租户刷新', '21050', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:cloudTenant:refresh', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21051');

-- 云租户绑定
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query_param,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21060', '云租户绑定', '21000', '7', 'binding', 'supply/orgTenantBinding/index', '',
    1, 0, 'C', '0', '0', 'supply:binding:list', 'tree',
    103, 1, now(), NULL, NULL, '云租户绑定菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21060');

INSERT INTO sys_menu SELECT '21061', '云租户绑定查询', '21060', '1', '#', '', '', 1, 0, 'F', '0', '0', 'supply:binding:query', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21061');
INSERT INTO sys_menu SELECT '21062', '云租户绑定新增', '21060', '2', '#', '', '', 1, 0, 'F', '0', '0', 'supply:binding:add', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21062');
INSERT INTO sys_menu SELECT '21063', '云租户绑定修改', '21060', '3', '#', '', '', 1, 0, 'F', '0', '0', 'supply:binding:edit', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21063');
INSERT INTO sys_menu SELECT '21064', '云租户绑定删除', '21060', '4', '#', '', '', 1, 0, 'F', '0', '0', 'supply:binding:remove', '#', 103, 1, now(), NULL, NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '21064');

-- 超级管理员默认授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT '1', sm.menu_id
FROM sys_menu sm
WHERE sm.menu_id IN (
    '21000','21001','21002','21003','21004','21005',
    '21010','21011','21012','21013','21014','21015','21016',
    '21020','21021','21022','21023','21024','21025',
    '21030','21031','21032','21033','21034','21035',
    '21040','21041','21042','21043','21044',
    '21050','21051',
    '21060','21061','21062','21063','21064'
)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu srm WHERE srm.role_id = '1' AND srm.menu_id = sm.menu_id
);

-- =========================
-- 供应链字典初始化
-- =========================

-- ROLLBACK: DELETE FROM sys_dict_data WHERE dict_code IN ('210001','210002','210003','210004','210011','210012','210013','210014','210015','210016','210021','210022','210023','210031','210032','210033','210034','210041','210042','210043','210044','210051','210052','210053','210054');
-- ROLLBACK: DELETE FROM sys_dict_type WHERE dict_id IN ('21001','21002','21003','21004','21005','21006');

INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT '21001', '000000', '供应商合作类型', 'supply_cooperation_type', 103, 1, now(), NULL, NULL, '供应链供应商合作类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE tenant_id = '000000' AND dict_type = 'supply_cooperation_type');
INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT '21002', '000000', '供应链设备类型', 'supply_device_type', 103, 1, now(), NULL, NULL, '供应链物理资源设备类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE tenant_id = '000000' AND dict_type = 'supply_device_type');
INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT '21003', '000000', '供应链资源池', 'supply_resource_pool', 103, 1, now(), NULL, NULL, '供应链资源池字典'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE tenant_id = '000000' AND dict_type = 'supply_resource_pool');
INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT '21004', '000000', '云平台类型', 'supply_cloud_platform_type', 103, 1, now(), NULL, NULL, '供应链云平台类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE tenant_id = '000000' AND dict_type = 'supply_cloud_platform_type');
INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT '21005', '000000', '供应链认证类型', 'supply_auth_type', 103, 1, now(), NULL, NULL, '供应链采集与事件接入认证类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE tenant_id = '000000' AND dict_type = 'supply_auth_type');
INSERT INTO sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT '21006', '000000', '供应链采集周期', 'supply_collect_cycle', 103, 1, now(), NULL, NULL, '供应链定时采集周期'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE tenant_id = '000000' AND dict_type = 'supply_collect_cycle');

INSERT INTO sys_dict_data SELECT '210001', '000000', 1, '公有云合作', 'public_cloud', 'supply_cooperation_type', '', 'primary', 'Y', 103, 1, now(), NULL, NULL, '公有云资源合作'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cooperation_type' AND dict_value = 'public_cloud');
INSERT INTO sys_dict_data SELECT '210002', '000000', 2, '私有云合作', 'private_cloud', 'supply_cooperation_type', '', 'success', 'N', 103, 1, now(), NULL, NULL, '私有云平台合作'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cooperation_type' AND dict_value = 'private_cloud');
INSERT INTO sys_dict_data SELECT '210003', '000000', 3, '硬件供货', 'hardware_supply', 'supply_cooperation_type', '', 'warning', 'N', 103, 1, now(), NULL, NULL, '硬件设备供货合作'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cooperation_type' AND dict_value = 'hardware_supply');
INSERT INTO sys_dict_data SELECT '210004', '000000', 4, '集成服务', 'integration_service', 'supply_cooperation_type', '', 'info', 'N', 103, 1, now(), NULL, NULL, '集成与运维服务合作'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cooperation_type' AND dict_value = 'integration_service');

INSERT INTO sys_dict_data SELECT '210011', '000000', 1, '服务器', 'server', 'supply_device_type', '', 'primary', 'Y', 103, 1, now(), NULL, NULL, '服务器'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_device_type' AND dict_value = 'server');
INSERT INTO sys_dict_data SELECT '210012', '000000', 2, '存储设备', 'storage', 'supply_device_type', '', 'success', 'N', 103, 1, now(), NULL, NULL, '存储设备'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_device_type' AND dict_value = 'storage');
INSERT INTO sys_dict_data SELECT '210013', '000000', 3, '交换机', 'switch', 'supply_device_type', '', 'warning', 'N', 103, 1, now(), NULL, NULL, '交换机'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_device_type' AND dict_value = 'switch');
INSERT INTO sys_dict_data SELECT '210014', '000000', 4, '防火墙', 'firewall', 'supply_device_type', '', 'danger', 'N', 103, 1, now(), NULL, NULL, '防火墙'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_device_type' AND dict_value = 'firewall');
INSERT INTO sys_dict_data SELECT '210015', '000000', 5, '负载均衡', 'load_balancer', 'supply_device_type', '', 'info', 'N', 103, 1, now(), NULL, NULL, '负载均衡设备'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_device_type' AND dict_value = 'load_balancer');
INSERT INTO sys_dict_data SELECT '210016', '000000', 99, '其他', 'other', 'supply_device_type', '', 'default', 'N', 103, 1, now(), NULL, NULL, '其他设备'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_device_type' AND dict_value = 'other');

INSERT INTO sys_dict_data SELECT '210021', '000000', 1, '上海生产资源池', 'pool-sh-prod', 'supply_resource_pool', '', 'primary', 'Y', 103, 1, now(), NULL, NULL, '联通云上海生产资源池'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_resource_pool' AND dict_value = 'pool-sh-prod');
INSERT INTO sys_dict_data SELECT '210022', '000000', 2, '北京生产资源池', 'pool-bj-prod', 'supply_resource_pool', '', 'success', 'N', 103, 1, now(), NULL, NULL, '联通云北京生产资源池'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_resource_pool' AND dict_value = 'pool-bj-prod');
INSERT INTO sys_dict_data SELECT '210023', '000000', 3, '广州灾备资源池', 'pool-gz-dr', 'supply_resource_pool', '', 'warning', 'N', 103, 1, now(), NULL, NULL, '联通云广州灾备资源池'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_resource_pool' AND dict_value = 'pool-gz-dr');

INSERT INTO sys_dict_data SELECT '210031', '000000', 1, '私有 OpenStack', 'private_openstack', 'supply_cloud_platform_type', '', 'primary', 'Y', 103, 1, now(), NULL, NULL, '私有 OpenStack 平台'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cloud_platform_type' AND dict_value = 'private_openstack');
INSERT INTO sys_dict_data SELECT '210032', '000000', 2, '私有 VMware', 'private_vmware', 'supply_cloud_platform_type', '', 'success', 'N', 103, 1, now(), NULL, NULL, '私有 VMware 平台'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cloud_platform_type' AND dict_value = 'private_vmware');
INSERT INTO sys_dict_data SELECT '210033', '000000', 3, '公有云', 'public_cloud', 'supply_cloud_platform_type', '', 'warning', 'N', 103, 1, now(), NULL, NULL, '公有云平台'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cloud_platform_type' AND dict_value = 'public_cloud');
INSERT INTO sys_dict_data SELECT '210034', '000000', 4, '混合云', 'hybrid_cloud', 'supply_cloud_platform_type', '', 'info', 'N', 103, 1, now(), NULL, NULL, '混合云平台'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_cloud_platform_type' AND dict_value = 'hybrid_cloud');

INSERT INTO sys_dict_data SELECT '210041', '000000', 1, 'AK/SK', 'ak_sk', 'supply_auth_type', '', 'primary', 'Y', 103, 1, now(), NULL, NULL, '访问密钥认证'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_auth_type' AND dict_value = 'ak_sk');
INSERT INTO sys_dict_data SELECT '210042', '000000', 2, '用户名密码', 'username_password', 'supply_auth_type', '', 'warning', 'N', 103, 1, now(), NULL, NULL, '用户名密码认证'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_auth_type' AND dict_value = 'username_password');
INSERT INTO sys_dict_data SELECT '210043', '000000', 3, 'Token', 'token', 'supply_auth_type', '', 'success', 'N', 103, 1, now(), NULL, NULL, 'Token 认证'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_auth_type' AND dict_value = 'token');
INSERT INTO sys_dict_data SELECT '210044', '000000', 4, '证书认证', 'certificate', 'supply_auth_type', '', 'info', 'N', 103, 1, now(), NULL, NULL, '证书认证'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_auth_type' AND dict_value = 'certificate');

INSERT INTO sys_dict_data SELECT '210051', '000000', 1, '1小时', '1h', 'supply_collect_cycle', '', 'primary', 'N', 103, 1, now(), NULL, NULL, '每1小时执行'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_collect_cycle' AND dict_value = '1h');
INSERT INTO sys_dict_data SELECT '210052', '000000', 2, '6小时', '6h', 'supply_collect_cycle', '', 'success', 'Y', 103, 1, now(), NULL, NULL, '每6小时执行，联通云 V1 推荐'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_collect_cycle' AND dict_value = '6h');
INSERT INTO sys_dict_data SELECT '210053', '000000', 3, '12小时', '12h', 'supply_collect_cycle', '', 'warning', 'N', 103, 1, now(), NULL, NULL, '每12小时执行'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_collect_cycle' AND dict_value = '12h');
INSERT INTO sys_dict_data SELECT '210054', '000000', 4, '24小时', '24h', 'supply_collect_cycle', '', 'info', 'N', 103, 1, now(), NULL, NULL, '每24小时执行'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = '000000' AND dict_type = 'supply_collect_cycle' AND dict_value = '24h');

-- =========================
-- 供应链管理员角色初始化
-- =========================

-- ROLLBACK: DELETE FROM sys_role_menu WHERE role_id IN (SELECT role_id FROM sys_role WHERE tenant_id = '000000' AND role_key = 'supply_admin');
-- ROLLBACK: DELETE FROM sys_role WHERE tenant_id = '000000' AND role_key = 'supply_admin';

INSERT INTO sys_role (
    role_id, tenant_id, role_name, role_key, role_sort, data_scope,
    menu_check_strictly, dept_check_strictly, parent_id, role_level,
    status, del_flag, create_dept, create_by, create_time, update_by, update_time, remark
)
SELECT
    '21000', '000000', '供应链管理员', 'supply_admin', 20, '1',
    't', 't', NULL, 0,
    '0', '0', 103, 1, now(), NULL, NULL, '供应链模块管理员角色'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE tenant_id = '000000' AND role_key = 'supply_admin'
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT sr.role_id, sm.menu_id
FROM sys_role sr
JOIN sys_menu sm ON sm.menu_id IN (
    '21000','21001','21002','21003','21004','21005',
    '21010','21011','21012','21013','21014','21015','21016',
    '21020','21021','21022','21023','21024','21025',
    '21030','21031','21032','21033','21034','21035',
    '21040','21041','21042','21043','21044',
    '21050','21051',
    '21060','21061','21062','21063','21064'
)
WHERE sr.tenant_id = '000000'
  AND sr.role_key = 'supply_admin'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu srm
      WHERE srm.role_id = sr.role_id AND srm.menu_id = sm.menu_id
  );
