package com.csmp.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色继承菜单隐藏表 sys_role_hidden_menu
 */
@Data
@TableName("sys_role_hidden_menu")
public class SysRoleHiddenMenu {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 被隐藏的继承菜单ID
     */
    private Long menuId;
}
