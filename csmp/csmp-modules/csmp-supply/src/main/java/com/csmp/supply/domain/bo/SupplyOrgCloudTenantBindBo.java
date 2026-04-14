package com.csmp.supply.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 组织绑定请求对象
 *
 * @author csmp
 */
@Data
public class SupplyOrgCloudTenantBindBo {

    @NotNull(message = "绑定ID不能为空", groups = EditGroup.class)
    private Long bindingId;
    @NotNull(message = "组织ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long orgId;
    @NotNull(message = "云平台ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long cloudPlatformId;
    @NotNull(message = "云租户快照ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long cloudTenantSnapshotId;
    private String bindStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidTime;
    private String bindingRemark;
}
