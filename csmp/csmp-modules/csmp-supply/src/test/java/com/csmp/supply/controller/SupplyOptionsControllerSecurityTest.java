package com.csmp.supply.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("dev")
class SupplyOptionsControllerSecurityTest {

    @Test
    void orgOptionsShouldRequireBindingListPermission() throws NoSuchMethodException {
        Method method = SupplyOptionsController.class.getMethod("orgOptions");
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);

        assertNotNull(permission, "组织选项接口必须声明权限校验");
        assertArrayEquals(new String[]{"supply:binding:list"}, permission.value());
    }

    @Test
    void cloudTenantOptionsShouldRequireBindingOrCloudTenantListPermission() throws NoSuchMethodException {
        Method method = SupplyOptionsController.class.getMethod("cloudTenantOptions", Long.class, String.class, String.class);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);

        assertNotNull(permission, "云租户选项接口必须声明权限校验");
        assertArrayEquals(new String[]{"supply:binding:list", "supply:cloudTenant:list"}, permission.value());
        assertEquals(SaMode.OR, permission.mode());
    }
}
