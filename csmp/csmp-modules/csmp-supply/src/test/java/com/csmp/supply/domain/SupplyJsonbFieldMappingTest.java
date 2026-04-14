package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.supply.support.PostgresJsonbStringTypeHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class SupplyJsonbFieldMappingTest {

    @Test
    void jsonbFieldsShouldUsePostgresJsonbStringTypeHandler() throws NoSuchFieldException {
        assertJsonbFieldMapped(SupplyPhysicalResource.class, "specPayload");
        assertJsonbFieldMapped(SupplyCollectConfig.class, "scopeFilter");
        assertJsonbFieldMapped(SupplyCollectConfig.class, "collectOptions");
        assertJsonbFieldMapped(SupplyCollectLog.class, "configSnapshot");
        assertJsonbFieldMapped(SupplyEventLog.class, "rawPayload");
        assertJsonbFieldMapped(SupplyEventLog.class, "normalizedPayload");
        assertJsonbFieldMapped(SupplyCloudTenant.class, "rawPayload");
    }

    private void assertJsonbFieldMapped(Class<?> entityClass, String fieldName) throws NoSuchFieldException {
        TableName tableName = entityClass.getAnnotation(TableName.class);
        assertNotNull(tableName, () -> entityClass.getSimpleName() + " 必须声明 @TableName");
        assertTrue(tableName.autoResultMap(), () -> entityClass.getSimpleName() + " 必须启用 autoResultMap");

        Field field = entityClass.getDeclaredField(fieldName);
        TableField tableField = field.getAnnotation(TableField.class);
        assertNotNull(tableField, () -> entityClass.getSimpleName() + "." + fieldName + " 必须声明 @TableField");
        assertEquals(PostgresJsonbStringTypeHandler.class, tableField.typeHandler(),
            () -> entityClass.getSimpleName() + "." + fieldName + " 必须使用 PostgresJsonbStringTypeHandler");
    }
}
