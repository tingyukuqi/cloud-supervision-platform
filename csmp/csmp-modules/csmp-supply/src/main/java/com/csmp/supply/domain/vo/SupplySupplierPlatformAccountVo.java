package com.csmp.supply.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 供应商平台账号响应对象
 *
 * @author csmp
 */
@Data
public class SupplySupplierPlatformAccountVo {

    private Long accountId;
    private Long supplierId;
    private Long cloudPlatformId;
    private String cloudPlatformName;
    private String accountName;
    private String accountType;
    private String accountIdentifier;
    private String accountStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastVerifiedTime;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
