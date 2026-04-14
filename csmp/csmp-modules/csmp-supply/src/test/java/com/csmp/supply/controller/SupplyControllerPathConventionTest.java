package com.csmp.supply.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class SupplyControllerPathConventionTest {

    @Test
    void supplyControllersShouldExposeStandardResourcePaths() {
        Map<Class<?>, String> expectedPaths = Map.of(
            SupplySupplierController.class, "/suppliers",
            SupplyPhysicalResourceController.class, "/physical-resources",
            SupplyCloudPlatformController.class, "/cloud-platforms",
            SupplyCollectConfigController.class, "/collect-configs",
            SupplyEventSubscriptionController.class, "/event-subscriptions",
            SupplyCloudTenantController.class, "/cloud-tenants",
            SupplyOrgCloudTenantBindController.class, "/org-tenant-bindings",
            SupplyOptionsController.class, "/options"
        );

        expectedPaths.forEach((controllerClass, expectedPath) -> {
            RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
            assertNotNull(mapping, controllerClass.getSimpleName() + " 必须声明类级别 RequestMapping");
            assertTrue(
                java.util.Arrays.asList(mapping.value()).contains(expectedPath),
                () -> controllerClass.getSimpleName() + " 必须包含标准资源路径 " + expectedPath
            );
        });
    }
}
