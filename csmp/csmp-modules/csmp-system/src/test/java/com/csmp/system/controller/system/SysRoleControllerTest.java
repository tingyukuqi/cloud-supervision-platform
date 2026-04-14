package com.csmp.system.controller.system;

import com.csmp.system.domain.SysRoleEffectiveMenu;
import com.csmp.system.domain.bo.SysRoleBo;
import com.csmp.system.service.IRoleEffectiveMenuService;
import com.csmp.system.service.ISysDeptService;
import com.csmp.system.service.ISysRoleService;
import com.csmp.system.service.ISysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SysRoleControllerTest {

    @Mock
    private ISysRoleService roleService;
    @Mock
    private ISysUserService userService;
    @Mock
    private ISysDeptService deptService;
    @Mock
    private IRoleEffectiveMenuService effectiveMenuService;

    @InjectMocks
    private SysRoleController controller;

    @Test
    void effectiveMenusShouldCheckRoleDataScope() {
        controller.effectiveMenus(9L);
        verify(roleService).checkRoleDataScope(9L);
    }

    @Test
    void hideMenusShouldCheckRoleDataScope() {
        controller.hideMenus(9L, new Long[]{1L, 2L});
        verify(roleService).checkRoleDataScope(9L);
    }

    @Test
    void restoreMenusShouldCheckRoleDataScope() {
        controller.restoreMenus(9L, new Long[]{1L, 2L});
        verify(roleService).checkRoleDataScope(9L);
    }
}
