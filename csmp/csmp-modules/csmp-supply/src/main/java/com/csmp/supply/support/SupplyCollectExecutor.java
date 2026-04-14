package com.csmp.supply.support;

import com.csmp.supply.domain.SupplyCollectConfig;

/**
 * 采集执行器
 *
 * @author csmp
 */
public interface SupplyCollectExecutor {

    SupplyCollectResult execute(SupplyCollectConfig config);
}
