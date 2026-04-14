-- ======================================================
-- 角色分级权限 DDL（MySQL）
-- ROLLBACK:
-- 1. DROP TABLE IF EXISTS sys_role_hidden_menu;
-- 2. DROP TABLE IF EXISTS sys_role_effective_menu;
-- 3. ALTER TABLE sys_role DROP COLUMN parent_id, DROP COLUMN role_level;
-- 4. DROP INDEX idx_sys_role_parent_id ON sys_role;
-- ======================================================

-- 1. sys_role 新增字段
ALTER TABLE sys_role
    ADD COLUMN parent_id BIGINT(20) DEFAULT NULL COMMENT '父角色ID，顶级角色为NULL' AFTER dept_check_strictly,
    ADD COLUMN role_level INT(4) DEFAULT 0 COMMENT '角色层级深度，顶级=0' AFTER parent_id;

-- 2. 角色继承菜单隐藏表
CREATE TABLE IF NOT EXISTS sys_role_hidden_menu (
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    menu_id BIGINT(20) NOT NULL COMMENT '被隐藏的继承菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色继承菜单隐藏表';

-- 3. 角色有效菜单物化表
CREATE TABLE IF NOT EXISTS sys_role_effective_menu (
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    menu_id BIGINT(20) NOT NULL COMMENT '有效菜单ID',
    source VARCHAR(16) NOT NULL COMMENT '来源：OWN=自有, INHERITED=继承',
    inherit_from_role_id BIGINT(20) DEFAULT NULL COMMENT '继承自哪个角色',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB COMMENT='角色有效菜单物化表';

-- 4. 索引
CREATE INDEX idx_sys_role_parent_id ON sys_role (parent_id);
CREATE INDEX idx_sys_role_effective_menu_role ON sys_role_effective_menu (role_id);
CREATE INDEX idx_sys_role_effective_menu_menu ON sys_role_effective_menu (menu_id);
CREATE INDEX idx_sys_role_hidden_menu_role ON sys_role_hidden_menu (role_id);

-- 5. 为现有角色初始化物化表（现有角色全部为顶级，有效菜单=自有菜单）
INSERT INTO sys_role_effective_menu (role_id, menu_id, source, inherit_from_role_id)
SELECT srm.role_id, srm.menu_id, 'OWN', NULL
FROM sys_role_menu srm
INNER JOIN sys_role sr ON sr.role_id = srm.role_id
WHERE sr.del_flag = '0';
