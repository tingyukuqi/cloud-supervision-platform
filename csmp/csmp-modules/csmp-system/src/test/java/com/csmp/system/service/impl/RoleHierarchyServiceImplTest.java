package com.csmp.system.service.impl;

import com.csmp.common.core.exception.ServiceException;
import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleDept;
import com.csmp.system.mapper.SysRoleDeptMapper;
import com.csmp.system.mapper.SysRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class RoleHierarchyServiceImplTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleDeptMapper roleDeptMapper;

    @InjectMocks
    private RoleHierarchyServiceImpl hierarchyService;

    @Test
    void validateDataScopeConstraintShouldRejectDeptAndChildOrSelfParentToAllChild() {
        SysRole parent = new SysRole();
        parent.setRoleId(10L);
        parent.setDataScope("6");
        when(roleMapper.selectById(10L)).thenReturn(parent);

        assertThrows(ServiceException.class,
            () -> hierarchyService.validateDataScopeConstraint(null, 10L, "1", null));
    }

    @Test
    void validateDataScopeConstraintShouldRejectCustomDeptIdsOutsideParentScope() {
        SysRole parent = new SysRole();
        parent.setRoleId(10L);
        parent.setDataScope("2");
        when(roleMapper.selectById(10L)).thenReturn(parent);

        SysRoleDept dept200 = new SysRoleDept();
        dept200.setDeptId(200L);
        SysRoleDept dept201 = new SysRoleDept();
        dept201.setDeptId(201L);
        when(roleDeptMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(dept200, dept201));

        assertThrows(ServiceException.class,
            () -> hierarchyService.validateDataScopeConstraint(null, 10L, "2", new Long[]{200L, 999L}));
    }

    @Test
    void validateDataScopeConstraintShouldAllowCustomSubset() {
        SysRole parent = new SysRole();
        parent.setRoleId(10L);
        parent.setDataScope("2");
        when(roleMapper.selectById(10L)).thenReturn(parent);

        SysRoleDept dept200 = new SysRoleDept();
        dept200.setDeptId(200L);
        SysRoleDept dept201 = new SysRoleDept();
        dept201.setDeptId(201L);
        when(roleDeptMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(dept200, dept201));

        assertDoesNotThrow(() -> hierarchyService.validateDataScopeConstraint(null, 10L, "2", new Long[]{200L}));
    }
}
