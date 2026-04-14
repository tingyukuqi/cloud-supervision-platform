package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 采集日志响应对象
 *
 * @author csmp
 */
@Data
public class SupplyCollectLogVo {

    private Long collectLogId;
    private Long collectConfigId;
    private Long cloudPlatformId;
    private String collectScope;
    private String executeMode;
    private String jobInstanceId;
    private String traceId;
    private Long triggerUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    private Long durationMs;
    private String resultStatus;
    private String syncStatus;
    private Integer resourceCount;
    private Integer cloudTenantCount;
    private Integer syncRecordCount;
    private Object configSnapshot;
    private String errorMessage;
}
