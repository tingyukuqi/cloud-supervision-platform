package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import com.csmp.supply.support.PostgresJsonbStringTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 云租户快照
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "supply_cloud_tenant", autoResultMap = true)
public class SupplyCloudTenant extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long cloudPlatformId;

    private String externalTenantId;

    private String externalParentId;

    private String cloudTenantName;

    private String cloudTenantCode;

    private String tenantStatus;

    private String regionCode;

    private String sourceAccountIdentifier;

    @TableField(typeHandler = PostgresJsonbStringTypeHandler.class)
    private String rawPayload;

    private Date lastSyncTime;

    private String syncStatus;

    @TableLogic
    private String delFlag;
}
