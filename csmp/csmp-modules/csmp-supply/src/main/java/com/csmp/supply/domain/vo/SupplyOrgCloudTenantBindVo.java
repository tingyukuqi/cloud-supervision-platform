package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 组织绑定响应对象
 *
 * @author csmp
 */
@Data
public class SupplyOrgCloudTenantBindVo {

    private Long bindingId;
    private Long orgId;
    private String orgName;
    private Long cloudPlatformId;
    private String cloudPlatformName;
    private Long cloudTenantSnapshotId;
    private String cloudTenantName;
    private String bindStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidTime;
    private String bindingRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
