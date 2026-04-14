package com.csmp.supply.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 采集执行结果
 *
 * @author csmp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyCollectResult {

    private boolean success;
    private int resourceCount;
    private int cloudTenantCount;
    private int syncRecordCount;
    private String message;

    public static SupplyCollectResult success(int resourceCount, int cloudTenantCount, int syncRecordCount) {
        return new SupplyCollectResult(true, resourceCount, cloudTenantCount, syncRecordCount, "执行成功");
    }

    public static SupplyCollectResult failed(String message) {
        return new SupplyCollectResult(false, 0, 0, 0, message);
    }
}
