package com.csmp.supply.api.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 采集执行参数
 *
 * @author csmp
 */
@Data
public class CollectExecuteBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台租户ID
     */
    private String tenantId;

    /**
     * 采集配置ID
     */
    private Long collectConfigId;

    /**
     * 执行模式：manual/scheduled
     */
    private String executeMode;

    /**
     * 触发用户ID
     */
    private Long triggerUserId;

    /**
     * 触发备注
     */
    private String operatorRemark;
}
