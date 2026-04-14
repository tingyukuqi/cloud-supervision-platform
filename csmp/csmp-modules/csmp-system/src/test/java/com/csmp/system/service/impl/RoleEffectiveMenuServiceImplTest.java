package com.csmp.system.service.impl;

import com.csmp.system.domain.SysRoleEffectiveMenu;
import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleMenu;
import com.csmp.system.mapper.SysRoleEffectiveMenuMapper;
import com.csmp.system.mapper.SysRoleHiddenMenuMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.mapper.SysRoleMenuMapper;
import com.csmp.system.mapper.SysUserRoleMapper;
import com.csmp.system.service.IRoleHierarchyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class RoleEffectiveMenuServiceImplTest {

    @Mock
    private SysRoleEffectiveMenuMapper effectiveMenuMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysRoleHiddenMenuMapper hiddenMenuMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private IRoleHierarchyService hierarchyService;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private RoleEffectiveMenuServiceImpl effectiveMenuService;

    @Test
    void refreshEffectiveMenuShouldMarkCurrentRoleMenuAsOwnWhenDuplicatedInParent() {
        SysRoleMenu parentMenu = new SysRoleMenu();
        parentMenu.setRoleId(1L);
        parentMenu.setMenuId(100L);
        SysRoleMenu selfMenu = new SysRoleMenu();
        selfMenu.setRoleId(2L);
        selfMenu.setMenuId(100L);

        when(hierarchyService.getAncestorChain(2L)).thenReturn(List.of(1L, 2L));
        SysRole parentRole = new SysRole();
        parentRole.setRoleId(1L);
        parentRole.setStatus("0");
        SysRole currentRole = new SysRole();
        currentRole.setRoleId(2L);
        currentRole.setStatus("0");
        when(roleMapper.selectById(1L)).thenReturn(parentRole);
        when(roleMapper.selectById(2L)).thenReturn(currentRole);
        when(roleMenuMapper.selectList(any()))
            .thenReturn(List.of(parentMenu))
            .thenReturn(List.of(selfMenu));
        when(hiddenMenuMapper.selectHiddenMenuIdsByRoleId(1L)).thenReturn(List.of());
        when(hiddenMenuMapper.selectHiddenMenuIdsByRoleId(2L)).thenReturn(List.of());

        effectiveMenuService.refreshEffectiveMenu(2L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SysRoleEffectiveMenu>> captor = ArgumentCaptor.forClass(List.class);
        verify(effectiveMenuMapper).insertBatch(captor.capture());
        List<SysRoleEffectiveMenu> records = captor.getValue();
        assertEquals(1, records.size());
        assertEquals("OWN", records.get(0).getSource());
        assertNull(records.get(0).getInheritFromRoleId());
    }

    @Test
    void refreshEffectiveMenuShouldKeepOwnMenuWhenSameMenuWasPreviouslyHiddenFromParent() {
        SysRoleMenu parentMenu = new SysRoleMenu();
        parentMenu.setRoleId(1L);
        parentMenu.setMenuId(100L);
        SysRoleMenu selfMenu = new SysRoleMenu();
        selfMenu.setRoleId(2L);
        selfMenu.setMenuId(100L);

        when(hierarchyService.getAncestorChain(2L)).thenReturn(List.of(1L, 2L));
        SysRole parentRole = new SysRole();
        parentRole.setRoleId(1L);
        parentRole.setStatus("0");
        SysRole currentRole = new SysRole();
        currentRole.setRoleId(2L);
        currentRole.setStatus("0");
        when(roleMapper.selectById(1L)).thenReturn(parentRole);
        when(roleMapper.selectById(2L)).thenReturn(currentRole);
        when(roleMenuMapper.selectList(any()))
            .thenReturn(List.of(parentMenu))
            .thenReturn(List.of(selfMenu));
        when(hiddenMenuMapper.selectHiddenMenuIdsByRoleId(1L)).thenReturn(List.of());
        when(hiddenMenuMapper.selectHiddenMenuIdsByRoleId(2L)).thenReturn(List.of(100L));

        effectiveMenuService.refreshEffectiveMenu(2L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SysRoleEffectiveMenu>> captor = ArgumentCaptor.forClass(List.class);
        verify(effectiveMenuMapper).insertBatch(captor.capture());
        List<SysRoleEffectiveMenu> records = captor.getValue();
        assertEquals(1, records.size());
        assertEquals("OWN", records.get(0).getSource());
        assertNull(records.get(0).getInheritFromRoleId());
    }
}
