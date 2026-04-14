package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 采集配置响应对象
 *
 * @author csmp
 */
@Data
public class SupplyCollectConfigVo {

    private Long collectConfigId;
    private Long cloudPlatformId;
    private String cloudPlatformName;
    private String providerCode;
    private String connectorCode;
    private String collectUrl;
    private String syncEndpoint;
    private String collectScope;
    private String collectMode;
    private String syncStrategy;
    private String authType;
    private Object authPayloadMasked;
    private Object scopeFilter;
    private Object collectOptions;
    private String executeCycle;
    private Integer timeoutSeconds;
    private Integer retryTimes;
    private Boolean verifySsl;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastCollectTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastSuccessTime;
    private String lastCollectStatus;
    private String lastErrorMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextCollectTime;
    private String remark;
}
