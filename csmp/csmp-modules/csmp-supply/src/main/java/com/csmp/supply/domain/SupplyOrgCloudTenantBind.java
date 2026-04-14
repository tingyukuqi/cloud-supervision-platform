package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 组织与云租户绑定
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_org_cloud_tenant_bind")
public class SupplyOrgCloudTenantBind extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long orgId;

    private Long cloudPlatformId;

    private Long cloudTenantSnapshotId;

    private String bindStatus;

    private Date effectiveTime;

    private Date invalidTime;

    private String bindingRemark;

    @TableLogic
    private String delFlag;
}
