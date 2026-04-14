package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 事件订阅响应对象
 *
 * @author csmp
 */
@Data
public class SupplyEventSubscriptionVo {

    private Long subscriptionId;
    private Long cloudPlatformId;
    private String cloudPlatformName;
    private String providerCode;
    private String eventScope;
    private String ingestMode;
    private String topicName;
    private String consumerGroup;
    private String endpointPath;
    private String authType;
    private Object authPayloadMasked;
    private String dataFormat;
    private String schemaVersion;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastEventTime;
    private String lastErrorMessage;
    private String remark;
}
