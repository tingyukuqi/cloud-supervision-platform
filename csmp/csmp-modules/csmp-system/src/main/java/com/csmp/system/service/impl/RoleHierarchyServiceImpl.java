package com.csmp.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.mybatis.enums.DataScopeType;
import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleDept;
import com.csmp.system.domain.vo.SysRoleVo;
import com.csmp.system.mapper.SysRoleDeptMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.service.IRoleHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 角色层级管理服务实现
 */
@RequiredArgsConstructor
@Service
public class RoleHierarchyServiceImpl implements IRoleHierarchyService {

    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    public List<SysRoleVo> selectRoleTree() {
        List<SysRoleVo> allRoles = roleMapper.selectRoleTree();
        return buildTree(allRoles);
    }

    @Override
    public void validateParent(Long roleId, Long parentId) {
        if (ObjectUtil.isNull(parentId)) {
            return;
        }
        if (parentId.equals(roleId)) {
            throw new ServiceException("不允许设置自己为父角色!");
        }
        // 检查循环依赖
        Set<Long> visited = new HashSet<>();
        Long currentId = parentId;
        while (ObjectUtil.isNotNull(currentId)) {
            if (currentId.equals(roleId)) {
                throw new ServiceException("不允许设置循环的父角色关系!");
            }
            if (!visited.add(currentId)) {
                throw new ServiceException("检测到角色层级循环，请检查数据!");
            }
            SysRole current = roleMapper.selectById(currentId);
            if (ObjectUtil.isNull(current)) {
                throw new ServiceException("父角色不存在!");
            }
            // 租户隔离
            if (ObjectUtil.isNotNull(roleId)) {
                SysRole self = roleMapper.selectById(roleId);
                if (ObjectUtil.isNotNull(self)
                    && !Objects.equals(self.getTenantId(), current.getTenantId())) {
                    throw new ServiceException("父角色不在同一租户内!");
                }
            }
            currentId = current.getParentId();
        }
    }

    @Override
    public List<Long> getDescendantRoleIds(Long roleId) {
        List<Long> result = new ArrayList<>();
        result.add(roleId);
        collectDescendants(roleId, result);
        return result;
    }

    @Override
    public List<Long> getAncestorChain(Long roleId) {
        LinkedList<Long> chain = new LinkedList<>();
        Long currentId = roleId;
        Set<Long> visited = new HashSet<>();
        while (ObjectUtil.isNotNull(currentId)) {
            if (!visited.add(currentId)) {
                break;
            }
            chain.addFirst(currentId);
            SysRole role = roleMapper.selectById(currentId);
            if (ObjectUtil.isNull(role)) {
                break;
            }
            currentId = role.getParentId();
        }
        return new ArrayList<>(chain);
    }

    @Override
    public void validateDataScopeConstraint(Long roleId, Long parentId, String dataScope, Long[] deptIds) {
        if (ObjectUtil.isNull(parentId) || ObjectUtil.isNull(dataScope)) {
            return;
        }
        SysRole parentRole = roleMapper.selectById(parentId);
        if (ObjectUtil.isNull(parentRole) || ObjectUtil.isNull(parentRole.getDataScope())) {
            return;
        }
        if (!isDataScopeAllowed(parentRole.getDataScope(), dataScope)) {
            throw new ServiceException(
                String.format("子角色数据权限范围不能超过父角色! 父角色数据范围: %s, 当前设置: %s",
                    getDataScopeName(parentRole.getDataScope()), getDataScopeName(dataScope)));
        }
        if (DataScopeType.CUSTOM.getCode().equals(parentRole.getDataScope())
            && DataScopeType.CUSTOM.getCode().equals(dataScope)) {
            validateCustomDeptSubset(parentId, deptIds);
        }
    }

    @Override
    public int calculateRoleLevel(Long parentId) {
        if (ObjectUtil.isNull(parentId)) {
            return 0;
        }
        SysRole parent = roleMapper.selectById(parentId);
        if (ObjectUtil.isNull(parent)) {
            return 0;
        }
        return parent.getRoleLevel() + 1;
    }

    private void collectDescendants(Long parentId, List<Long> result) {
        List<Long> childIds = roleMapper.selectChildRoleIds(parentId);
        for (Long childId : childIds) {
            result.add(childId);
            collectDescendants(childId, result);
        }
    }

    private List<SysRoleVo> buildTree(List<SysRoleVo> allRoles) {
        Map<Long, SysRoleVo> roleMap = new LinkedHashMap<>();
        for (SysRoleVo role : allRoles) {
            role.setChildren(new ArrayList<>());
            roleMap.put(role.getRoleId(), role);
        }
        List<SysRoleVo> roots = new ArrayList<>();
        for (SysRoleVo role : allRoles) {
            if (ObjectUtil.isNull(role.getParentId()) || !roleMap.containsKey(role.getParentId())) {
                roots.add(role);
            } else {
                SysRoleVo parent = roleMap.get(role.getParentId());
                parent.getChildren().add(role);
            }
        }
        return roots;
    }

    private boolean isDataScopeAllowed(String parentScope, String childScope) {
        if (DataScopeType.ALL.getCode().equals(parentScope)) {
            return true;
        }
        if (DataScopeType.SELF.getCode().equals(parentScope)) {
            return DataScopeType.SELF.getCode().equals(childScope);
        }
        if (DataScopeType.DEPT.getCode().equals(parentScope)) {
            return DataScopeType.SELF.getCode().equals(childScope)
                || DataScopeType.DEPT.getCode().equals(childScope);
        }
        if (DataScopeType.DEPT_AND_CHILD.getCode().equals(parentScope)) {
            return DataScopeType.DEPT.getCode().equals(childScope)
                || DataScopeType.SELF.getCode().equals(childScope)
                || DataScopeType.DEPT_AND_CHILD.getCode().equals(childScope)
                || DataScopeType.DEPT_AND_CHILD_OR_SELF.getCode().equals(childScope);
        }
        if (DataScopeType.CUSTOM.getCode().equals(parentScope)) {
            return !DataScopeType.ALL.getCode().equals(childScope);
        }
        if (DataScopeType.DEPT_AND_CHILD_OR_SELF.getCode().equals(parentScope)) {
            return DataScopeType.DEPT.getCode().equals(childScope)
                || DataScopeType.DEPT_AND_CHILD.getCode().equals(childScope)
                || DataScopeType.SELF.getCode().equals(childScope)
                || DataScopeType.DEPT_AND_CHILD_OR_SELF.getCode().equals(childScope);
        }
        return true;
    }

    private void validateCustomDeptSubset(Long parentId, Long[] deptIds) {
        if (deptIds == null || deptIds.length == 0) {
            throw new ServiceException("自定义数据权限必须选择部门范围!");
        }
        Set<Long> parentDeptIds = new HashSet<>();
        for (SysRoleDept roleDept : roleDeptMapper.selectList(new QueryWrapper<SysRoleDept>()
            .select("dept_id")
            .eq("role_id", parentId))) {
            parentDeptIds.add(roleDept.getDeptId());
        }
        for (Long deptId : deptIds) {
            if (!parentDeptIds.contains(deptId)) {
                throw new ServiceException("自定义数据权限范围必须是父角色范围的子集!");
            }
        }
    }

    private String getDataScopeName(String code) {
        if (DataScopeType.ALL.getCode().equals(code)) return "全部数据";
        if (DataScopeType.CUSTOM.getCode().equals(code)) return "自定义";
        if (DataScopeType.DEPT.getCode().equals(code)) return "本部门";
        if (DataScopeType.DEPT_AND_CHILD.getCode().equals(code)) return "本部门及子部门";
        if (DataScopeType.SELF.getCode().equals(code)) return "仅本人";
        if (DataScopeType.DEPT_AND_CHILD_OR_SELF.getCode().equals(code)) return "部门及子部门或本人";
        return code;
    }
}
