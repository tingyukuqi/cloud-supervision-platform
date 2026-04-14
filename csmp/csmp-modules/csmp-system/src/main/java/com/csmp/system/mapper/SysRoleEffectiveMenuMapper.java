package com.csmp.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csmp.common.mybatis.core.mapper.BaseMapperPlus;
import com.csmp.system.domain.SysRoleEffectiveMenu;

import java.util.List;

/**
 * 角色有效菜单 数据层
 */
public interface SysRoleEffectiveMenuMapper extends BaseMapperPlus<SysRoleEffectiveMenu, SysRoleEffectiveMenu> {

    /**
     * 查询角色的有效菜单ID列表
     */
    default List<Long> selectEffectiveMenuIdsByRoleId(Long roleId) {
        return this.selectObjs(new LambdaQueryWrapper<SysRoleEffectiveMenu>()
            .select(SysRoleEffectiveMenu::getMenuId)
            .eq(SysRoleEffectiveMenu::getRoleId, roleId));
    }

    /**
     * 查询角色的有效菜单来源详情
     */
    default List<SysRoleEffectiveMenu> selectEffectiveMenuDetailByRoleId(Long roleId) {
        return this.selectList(new LambdaQueryWrapper<SysRoleEffectiveMenu>()
            .eq(SysRoleEffectiveMenu::getRoleId, roleId));
    }
}
