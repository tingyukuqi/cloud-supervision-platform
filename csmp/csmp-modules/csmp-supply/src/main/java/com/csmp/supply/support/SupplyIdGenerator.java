package com.csmp.supply.support;

import com.csmp.common.mybatis.utils.IdGeneratorUtil;
import org.springframework.stereotype.Component;

/**
 * 供应链主键与追踪号生成器
 *
 * @author csmp
 */
@Component
public class SupplyIdGenerator {

    public Long nextId() {
        return IdGeneratorUtil.nextLongId();
    }

    public String nextTraceId() {
        return IdGeneratorUtil.nextUUID();
    }
}
