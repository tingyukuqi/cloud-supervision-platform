package com.csmp.system.service.impl;

import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleEffectiveMenu;
import com.csmp.system.domain.bo.SysRoleBo;
import com.csmp.system.domain.vo.SysRoleVo;
import com.csmp.system.event.RolePermissionChangedEvent;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.system.mapper.SysRoleDeptMapper;
import com.csmp.system.mapper.SysRoleEffectiveMenuMapper;
import com.csmp.system.mapper.SysRoleHiddenMenuMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.mapper.SysRoleMenuMapper;
import com.csmp.system.mapper.SysUserRoleMapper;
import com.csmp.system.service.IRoleEffectiveMenuService;
import com.csmp.system.service.IRoleHierarchyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysRoleDeptMapper roleDeptMapper;
    @Mock
    private SysRoleHiddenMenuMapper hiddenMenuMapper;
    @Mock
    private SysRoleEffectiveMenuMapper effectiveMenuMapper;
    @Mock
    private IRoleHierarchyService hierarchyService;
    @Mock
    private IRoleEffectiveMenuService effectiveMenuService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SysRoleServiceImpl roleService;

    @Test
    void selectRoleListShouldEnrichParentRoleName() {
        SysRoleVo parent = new SysRoleVo();
        parent.setRoleId(1L);
        parent.setRoleName("超级管理员");

        SysRoleVo child = new SysRoleVo();
        child.setRoleId(2L);
        child.setParentId(1L);
        child.setRoleName("部门管理员");

        when(roleMapper.selectRoleList(any())).thenReturn(List.of(parent, child));

        SysRole parentEntity = new SysRole();
        parentEntity.setRoleId(1L);
        parentEntity.setRoleName("超级管理员");
        when(roleMapper.selectByIds(List.of(1L))).thenReturn(List.of(parentEntity));

        List<SysRoleVo> result = roleService.selectRoleList(new SysRoleBo());

        assertNull(result.get(0).getParentRoleName());
        assertEquals("超级管理员", result.get(1).getParentRoleName());
    }

    @Test
    void selectPageRoleListShouldEnrichParentRoleName() {
        SysRoleVo child = new SysRoleVo();
        child.setRoleId(2L);
        child.setParentId(1L);
        child.setRoleName("部门管理员");

        Page<SysRoleVo> page = new Page<>(1, 10);
        page.setRecords(List.of(child));
        page.setTotal(1);
        when(roleMapper.selectPageRoleList(any(), any())).thenReturn(page);

        SysRole parentEntity = new SysRole();
        parentEntity.setRoleId(1L);
        parentEntity.setRoleName("超级管理员");
        when(roleMapper.selectByIds(List.of(1L))).thenReturn(List.of(parentEntity));

        TableDataInfo<SysRoleVo> result = roleService.selectPageRoleList(new SysRoleBo(), new PageQuery());

        assertEquals(1, result.getRows().size());
        assertEquals("超级管理员", result.getRows().get(0).getParentRoleName());
    }

    @Test
    void selectRoleByIdShouldEnrichHiddenAndInheritedMenus() {
        SysRoleVo roleVo = new SysRoleVo();
        roleVo.setRoleId(9L);
        roleVo.setParentId(3L);
        when(roleMapper.selectRoleById(9L)).thenReturn(roleVo);

        SysRole parent = new SysRole();
        parent.setRoleId(3L);
        parent.setRoleName("父角色");
        when(roleMapper.selectById(3L)).thenReturn(parent);
        when(hiddenMenuMapper.selectHiddenMenuIdsByRoleId(9L)).thenReturn(List.of(10L, 11L));

        SysRoleEffectiveMenu own = new SysRoleEffectiveMenu();
        own.setMenuId(100L);
        own.setSource("OWN");
        SysRoleEffectiveMenu inherited = new SysRoleEffectiveMenu();
        inherited.setMenuId(101L);
        inherited.setSource("INHERITED");
        when(effectiveMenuMapper.selectEffectiveMenuDetailByRoleId(9L)).thenReturn(List.of(own, inherited));

        SysRoleVo result = roleService.selectRoleById(9L);

        assertEquals("父角色", result.getParentRoleName());
        assertArrayEquals(new Long[]{10L, 11L}, result.getHiddenMenuIds());
        assertArrayEquals(new Long[]{101L}, result.getInheritedMenuIds());
    }

    @Test
    void updateRoleStatusShouldPublishCascadeRefreshEvent() {
        when(roleMapper.update(any(), any())).thenReturn(1);

        roleService.updateRoleStatus(9L, "0");

        verify(eventPublisher).publishEvent(any(RolePermissionChangedEvent.class));
    }
}
