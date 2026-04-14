package com.csmp.supply.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 物理资源导入模板
 *
 * @author csmp
 */
@Data
public class SupplyPhysicalResourceImportVo {

    @ExcelProperty("资源编号")
    @NotBlank(message = "资源编号不能为空")
    private String resourceCode;

    @ExcelProperty("供应商ID")
    private Long supplierId;

    @ExcelProperty("设备名称")
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @ExcelProperty("设备类型")
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @ExcelProperty("设备型号")
    private String deviceModel;

    @ExcelProperty("序列号")
    @NotBlank(message = "序列号不能为空")
    private String serialNumber;

    @ExcelProperty("资产标签")
    private String assetTag;

    @ExcelProperty("资源状态")
    @NotBlank(message = "资源状态不能为空")
    private String resourceStatus;

    @ExcelProperty("机柜位置")
    private String rackLocation;

    @ExcelProperty("机房位置")
    private String idcLocation;

    @ExcelProperty("管理IP")
    private String manageIp;

    @ExcelProperty("采购日期")
    private String purchaseDate;

    @ExcelProperty("到期日期")
    private String expireDate;

    @ExcelProperty("规格扩展JSON")
    private String specPayload;

    @ExcelProperty("备注")
    private String remark;
}
