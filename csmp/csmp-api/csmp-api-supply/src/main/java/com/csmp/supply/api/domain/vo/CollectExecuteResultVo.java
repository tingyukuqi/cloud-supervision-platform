package com.csmp.supply.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 采集执行结果
 *
 * @author csmp
 */
@Data
public class CollectExecuteResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否已受理
     */
    private Boolean accepted;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 任务实例ID
     */
    private String jobInstanceId;

    /**
     * 执行结果消息
     */
    private String message;
}
