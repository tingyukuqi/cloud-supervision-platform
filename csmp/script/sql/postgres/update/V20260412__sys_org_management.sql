-- =====================================================
-- 组织机构管理菜单
-- ROLLBACK: DELETE FROM sys_menu WHERE menu_id IN (2000, 2001, 2002, 2003, 2004, 2005); UPDATE sys_menu SET order_num = '4' WHERE menu_id = '103'; UPDATE sys_menu SET order_num = '5' WHERE menu_id = '104'; UPDATE sys_menu SET order_num = '6' WHERE menu_id = '105'; UPDATE sys_menu SET order_num = '7' WHERE menu_id = '106'; UPDATE sys_menu SET order_num = '8' WHERE menu_id = '107'; UPDATE sys_menu SET order_num = '9' WHERE menu_id = '108'; UPDATE sys_menu SET order_num = '10' WHERE menu_id = '118'; UPDATE sys_menu SET order_num = '11' WHERE menu_id = '123';
-- =====================================================

-- 调整系统管理下现有菜单排序，给组织机构菜单预留位置
update sys_menu set order_num = '5' where menu_id = '103';
update sys_menu set order_num = '6' where menu_id = '104';
update sys_menu set order_num = '7' where menu_id = '105';
update sys_menu set order_num = '8' where menu_id = '106';
update sys_menu set order_num = '9' where menu_id = '107';
update sys_menu set order_num = '10' where menu_id = '108';
update sys_menu set order_num = '11' where menu_id = '118';
update sys_menu set order_num = '12' where menu_id = '123';

-- 组织机构管理菜单（放在系统管理目录下，排序在部门管理之前）
insert into sys_menu values('2000', '组织机构', '1', '4', 'org', 'system/org/index', '', 1, 0, 'C', '0', '0', 'system:org:list', 'tree', 103, 1, now(), null, null, '组织机构管理菜单');

-- 组织机构查询
insert into sys_menu values('2001', '组织查询', '2000', '1', '', '', '', 1, 0, 'F', '0', '0', 'system:org:query',  '#', 103, 1, now(), null, null, '');
-- 组织机构新增
insert into sys_menu values('2002', '组织新增', '2000', '2', '', '', '', 1, 0, 'F', '0', '0', 'system:org:add',    '#', 103, 1, now(), null, null, '');
-- 组织机构修改
insert into sys_menu values('2003', '组织修改', '2000', '3', '', '', '', 1, 0, 'F', '0', '0', 'system:org:edit',   '#', 103, 1, now(), null, null, '');
-- 组织机构删除
insert into sys_menu values('2004', '组织删除', '2000', '4', '', '', '', 1, 0, 'F', '0', '0', 'system:org:remove', '#', 103, 1, now(), null, null, '');
-- 组织机构导出
insert into sys_menu values('2005', '组织导出', '2000', '5', '', '', '', 1, 0, 'F', '0', '0', 'system:org:export', '#', 103, 1, now(), null, null, '');
