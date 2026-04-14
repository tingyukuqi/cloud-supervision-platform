package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 云租户响应对象
 *
 * @author csmp
 */
@Data
public class SupplyCloudTenantVo {

    private Long cloudTenantSnapshotId;
    private Long cloudPlatformId;
    private String cloudPlatformName;
    private String externalTenantId;
    private String externalParentId;
    private String cloudTenantCode;
    private String cloudTenantName;
    private String tenantStatus;
    private String regionCode;
    private String sourceAccountIdentifier;
    private Object rawPayload;
    private String syncStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastSyncTime;
    private String bindStatus;
    private Long boundOrgId;
    private String boundOrgName;
}
