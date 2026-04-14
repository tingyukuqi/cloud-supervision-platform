package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 物理资源响应对象
 *
 * @author csmp
 */
@Data
public class SupplyPhysicalResourceVo {

    private Long resourceId;
    private String resourceCode;
    private Long supplierId;
    private String supplierName;
    private String deviceName;
    private String deviceType;
    private String deviceModel;
    private String serialNumber;
    private String assetTag;
    private String resourceStatus;
    private String rackLocation;
    private String idcLocation;
    private String manageIp;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date purchaseDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;
    private Object specPayload;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
