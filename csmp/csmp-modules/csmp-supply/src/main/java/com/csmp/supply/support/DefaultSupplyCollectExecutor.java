package com.csmp.supply.support;

import com.csmp.supply.domain.SupplyCollectConfig;
import org.springframework.stereotype.Component;

/**
 * 默认采集执行器
 *
 * <p>V1 先交付主链路与落库闭环，真实云厂商连接器后续按联通云、移动云、电信云顺序补齐。</p>
 *
 * @author csmp
 */
@Component
public class DefaultSupplyCollectExecutor implements SupplyCollectExecutor {

    @Override
    public SupplyCollectResult execute(SupplyCollectConfig config) {
        return SupplyCollectResult.success(0, "tenant".equals(config.getCollectScope()) ? 1 : 0, 0);
    }
}
