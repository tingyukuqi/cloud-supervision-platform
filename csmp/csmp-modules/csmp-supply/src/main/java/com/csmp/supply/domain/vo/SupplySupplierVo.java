package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 供应商响应对象
 *
 * @author csmp
 */
@Data
public class SupplySupplierVo {

    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private String supplierShortName;
    private String supplierType;
    private String creditCode;
    private String serviceScope;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String cooperationType;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date onboardTime;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date offboardTime;
    private Integer platformCount;
    private Integer accountCount;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
