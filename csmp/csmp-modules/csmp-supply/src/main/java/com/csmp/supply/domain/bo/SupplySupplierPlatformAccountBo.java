package com.csmp.supply.domain.bo;

import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 供应商平台账号请求对象
 *
 * @author csmp
 */
@Data
public class SupplySupplierPlatformAccountBo {

    @NotNull(message = "账号ID不能为空", groups = EditGroup.class)
    private Long accountId;

    @NotNull(message = "供应商ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long supplierId;

    private Long cloudPlatformId;

    @NotBlank(message = "账号名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "账号名称长度不能超过128")
    private String accountName;

    @Size(max = 32, message = "账号类型长度不能超过32")
    private String accountType;

    @NotBlank(message = "账号标识不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "账号标识长度不能超过128")
    private String accountIdentifier;

    private String accountStatus;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
