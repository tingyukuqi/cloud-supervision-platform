package com.csmp.supply.domain.bo;

import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 云平台请求对象
 *
 * @author csmp
 */
@Data
public class SupplyCloudPlatformBo {

    @NotNull(message = "平台ID不能为空", groups = EditGroup.class)
    private Long platformId;
    @NotBlank(message = "平台编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String platformCode;
    @NotBlank(message = "平台名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String platformName;
    @NotBlank(message = "平台类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String platformType;
    @NotBlank(message = "提供商编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String providerCode;
    private String resourcePoolCode;
    private String regionCode;
    @NotBlank(message = "访问地址不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 255, message = "访问地址长度不能超过255")
    private String accessUrl;
    private String apiVersion;
    private String description;
    private String status;
}
