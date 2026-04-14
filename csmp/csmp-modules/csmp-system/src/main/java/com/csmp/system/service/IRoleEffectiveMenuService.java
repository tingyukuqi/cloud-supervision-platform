package com.csmp.system.service;

import java.util.Set;

/**
 * 角色有效菜单管理服务
 */
public interface IRoleEffectiveMenuService {

    /**
     * 刷新单个角色的有效菜单物化数据
     */
    void refreshEffectiveMenu(Long roleId);

    /**
     * 刷新角色及其所有子孙角色的有效菜单
     */
    void refreshEffectiveMenuCascade(Long roleId);

    /**
     * 全量刷新所有角色的有效菜单
     */
    void refreshAllEffectiveMenus();

    /**
     * 查询角色的有效菜单ID集合
     */
    Set<Long> getEffectiveMenuIds(Long roleId);
}
