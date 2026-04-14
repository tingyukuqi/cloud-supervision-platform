package com.csmp.supply.domain.bo;

import lombok.Data;

/**
 * 云租户查询对象
 *
 * @author csmp
 */
@Data
public class SupplyCloudTenantBo {

    private Long cloudPlatformId;
    private String keyword;
    private String tenantStatus;
    private String bindStatus;
}
