package com.csmp.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色有效菜单物化表 sys_role_effective_menu
 */
@Data
@TableName("sys_role_effective_menu")
public class SysRoleEffectiveMenu {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 有效菜单ID
     */
    private Long menuId;

    /**
     * 来源：OWN=自有, INHERITED=继承
     */
    private String source;

    /**
     * 继承自哪个角色
     */
    private Long inheritFromRoleId;
}
