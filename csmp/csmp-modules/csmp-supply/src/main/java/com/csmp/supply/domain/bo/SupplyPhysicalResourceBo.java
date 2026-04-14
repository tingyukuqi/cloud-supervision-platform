package com.csmp.supply.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.json.validate.JsonPattern;
import com.csmp.common.json.validate.JsonType;
import com.csmp.supply.support.JsonObjectStringDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * 物理资源请求对象
 *
 * @author csmp
 */
@Data
public class SupplyPhysicalResourceBo {

    @NotNull(message = "资源ID不能为空", groups = EditGroup.class)
    private Long resourceId;
    private Long supplierId;
    @NotBlank(message = "资源编号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String resourceCode;
    @NotBlank(message = "设备名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String deviceName;
    @NotBlank(message = "设备类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String deviceType;
    private String deviceModel;
    @NotBlank(message = "序列号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String serialNumber;
    private String assetTag;
    @NotBlank(message = "资源状态不能为空", groups = {AddGroup.class, EditGroup.class})
    private String resourceStatus;
    private String rackLocation;
    private String idcLocation;
    @Pattern(regexp = "^$|^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(?!$)|$)){4}$", message = "管理IP格式不正确")
    private String manageIp;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date purchaseDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;
    @JsonDeserialize(using = JsonObjectStringDeserializer.class)
    @JsonPattern(type = JsonType.OBJECT, message = "规格扩展必须为JSON对象")
    private String specPayload;
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
    private String beginCreateTime;
    private String endCreateTime;
    private String keyword;
}
