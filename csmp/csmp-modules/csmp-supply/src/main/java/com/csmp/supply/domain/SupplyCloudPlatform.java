package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 云平台主数据
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_cloud_platform")
public class SupplyCloudPlatform extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String platformCode;

    private String platformName;

    private String platformType;

    private String providerCode;

    private String resourcePoolCode;

    private String regionCode;

    private String accessUrl;

    private String apiVersion;

    private String description;

    private String status;

    @TableLogic
    private String delFlag;
}
