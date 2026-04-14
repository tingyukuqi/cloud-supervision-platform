package com.csmp.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csmp.common.mybatis.core.mapper.BaseMapperPlus;
import com.csmp.system.domain.SysRoleHiddenMenu;

import java.util.List;

/**
 * 角色隐藏菜单 数据层
 */
public interface SysRoleHiddenMenuMapper extends BaseMapperPlus<SysRoleHiddenMenu, SysRoleHiddenMenu> {

    /**
     * 根据角色ID查询隐藏的菜单ID列表
     */
    default List<Long> selectHiddenMenuIdsByRoleId(Long roleId) {
        return this.selectObjs(new LambdaQueryWrapper<SysRoleHiddenMenu>()
            .select(SysRoleHiddenMenu::getMenuId)
            .eq(SysRoleHiddenMenu::getRoleId, roleId));
    }
}
