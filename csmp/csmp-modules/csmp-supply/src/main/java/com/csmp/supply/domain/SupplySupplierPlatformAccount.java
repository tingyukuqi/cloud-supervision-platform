package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 供应商平台账号
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_supplier_platform_account")
public class SupplySupplierPlatformAccount extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long supplierId;

    private Long cloudPlatformId;

    private String accountName;

    private String accountType;

    private String accountIdentifier;

    private String accountStatus;

    private Date lastVerifiedTime;

    private String remark;

    @TableLogic
    private String delFlag;
}
