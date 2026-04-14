package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 事件日志响应对象
 *
 * @author csmp
 */
@Data
public class SupplyEventLogVo {

    private Long eventLogId;
    private Long subscriptionId;
    private Long cloudPlatformId;
    private String eventScope;
    private String eventKey;
    private String sourceEventId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ingestTime;
    private String processStatus;
    private String traceId;
    private Object rawPayload;
    private Object normalizedPayload;
    private String errorMessage;
}
