package com.csmp.supply.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * 供应商请求对象
 *
 * @author csmp
 */
@Data
public class SupplySupplierBo {

    @NotNull(message = "供应商ID不能为空", groups = EditGroup.class)
    private Long supplierId;

    @NotBlank(message = "供应商编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "供应商编码长度不能超过64")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "供应商名称长度不能超过64")
    private String supplierName;

    @Size(max = 64, message = "供应商简称长度不能超过64")
    private String supplierShortName;

    @NotBlank(message = "供应商类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String supplierType;

    @Size(max = 32, message = "统一社会信用代码长度不能超过32")
    private String creditCode;

    @Size(max = 255, message = "服务范围长度不能超过255")
    private String serviceScope;

    @Size(max = 64, message = "联系人长度不能超过64")
    private String contactName;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "联系电话格式不正确")
    private String contactPhone;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "联系邮箱格式不正确")
    private String contactEmail;

    @Size(max = 255, message = "地址长度不能超过255")
    private String address;

    private String cooperationType;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date onboardTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date offboardTime;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
