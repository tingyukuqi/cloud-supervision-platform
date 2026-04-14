-- ======================================================
-- 角色分级权限 DDL
-- ROLLBACK: DROP TABLE IF EXISTS sys_role_hidden_menu, sys_role_effective_menu;
--           ALTER TABLE sys_role DROP COLUMN IF EXISTS parent_id, role_level;
-- ======================================================

-- 1. sys_role 新增字段
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS parent_id BIGINT DEFAULT NULL;
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS role_level INT DEFAULT 0;

COMMENT ON COLUMN sys_role.parent_id IS '父角色ID，顶级角色为NULL';
COMMENT ON COLUMN sys_role.role_level IS '角色层级深度，顶级=0';

-- 2. 角色继承菜单隐藏表
CREATE TABLE IF NOT EXISTS sys_role_hidden_menu (
    role_id  BIGINT NOT NULL,
    menu_id  BIGINT NOT NULL,
    CONSTRAINT pk_role_hidden_menu PRIMARY KEY (role_id, menu_id)
);

COMMENT ON TABLE sys_role_hidden_menu IS '角色继承菜单隐藏表';
COMMENT ON COLUMN sys_role_hidden_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_hidden_menu.menu_id IS '被隐藏的继承菜单ID';

-- 3. 角色有效菜单物化表
CREATE TABLE IF NOT EXISTS sys_role_effective_menu (
    role_id              BIGINT NOT NULL,
    menu_id              BIGINT NOT NULL,
    source               VARCHAR(16) NOT NULL,
    inherit_from_role_id BIGINT DEFAULT NULL,
    CONSTRAINT pk_role_effective_menu PRIMARY KEY (role_id, menu_id)
);

ALTER TABLE sys_role_hidden_menu DROP COLUMN IF EXISTS tenant_id;
ALTER TABLE sys_role_effective_menu DROP COLUMN IF EXISTS tenant_id;

COMMENT ON TABLE sys_role_effective_menu IS '角色有效菜单物化表';
COMMENT ON COLUMN sys_role_effective_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_effective_menu.menu_id IS '有效菜单ID';
COMMENT ON COLUMN sys_role_effective_menu.source IS '来源：OWN=自有, INHERITED=继承';
COMMENT ON COLUMN sys_role_effective_menu.inherit_from_role_id IS '继承自哪个角色';

-- 4. 索引
CREATE INDEX IF NOT EXISTS idx_sys_role_parent_id ON sys_role (parent_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_effective_menu_role ON sys_role_effective_menu (role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_effective_menu_menu ON sys_role_effective_menu (menu_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_hidden_menu_role ON sys_role_hidden_menu (role_id);

-- 5. 为现有角色初始化物化表（现有角色全部为顶级，有效菜单=自有菜单）
INSERT INTO sys_role_effective_menu (role_id, menu_id, source, inherit_from_role_id)
SELECT srm.role_id, srm.menu_id, 'OWN', NULL
FROM sys_role_menu srm
INNER JOIN sys_role sr ON sr.role_id = srm.role_id
WHERE sr.del_flag = '0';
