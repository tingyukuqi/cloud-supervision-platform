package com.csmp.system.service.impl;

import com.csmp.system.domain.SysMenu;
import com.csmp.system.domain.vo.SysMenuVo;
import com.csmp.system.mapper.SysMenuMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.mapper.SysRoleMenuMapper;
import com.csmp.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SysMenuServiceImplTest {

    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysTenantPackageMapper tenantPackageMapper;

    @InjectMocks
    private SysMenuServiceImpl menuService;

    @Test
    void selectMenuListShouldUseEffectiveMenuSqlForNormalUser() {
        Long userId = 2L;
        when(menuMapper.buildEffectiveMenuByUserSql(userId)).thenReturn("select 1");
        when(menuMapper.selectVoList(ArgumentMatchers.any())).thenReturn(List.of(new SysMenuVo()));

        menuService.selectMenuList(userId);

        verify(menuMapper).buildEffectiveMenuByUserSql(userId);
    }

    @Test
    void selectMenuTreeByUserIdShouldUseEffectiveMenuSqlForNormalUser() {
        Long userId = 2L;
        when(menuMapper.buildEffectiveMenuByUserSql(userId)).thenReturn("select 1");
        when(menuMapper.selectList(ArgumentMatchers.any())).thenReturn(List.<SysMenu>of());

        menuService.selectMenuTreeByUserId(userId);

        verify(menuMapper).buildEffectiveMenuByUserSql(userId);
    }
}
