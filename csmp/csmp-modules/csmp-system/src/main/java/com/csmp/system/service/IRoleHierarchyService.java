package com.csmp.system.service;

import com.csmp.system.domain.vo.SysRoleVo;

import java.util.List;

/**
 * 角色层级管理服务
 */
public interface IRoleHierarchyService {

    /**
     * 获取角色树形结构
     */
    List<SysRoleVo> selectRoleTree();

    /**
     * 校验父角色设置是否合法（防止循环依赖、跨租户）
     */
    void validateParent(Long roleId, Long parentId);

    /**
     * 递归获取所有子孙角色ID
     */
    List<Long> getDescendantRoleIds(Long roleId);

    /**
     * 递归获取祖先角色链（从顶级到当前角色）
     */
    List<Long> getAncestorChain(Long roleId);

    /**
     * 校验数据权限继承约束
     */
    void validateDataScopeConstraint(Long roleId, Long parentId, String dataScope, Long[] deptIds);

    /**
     * 计算角色层级深度
     */
    int calculateRoleLevel(Long parentId);
}
