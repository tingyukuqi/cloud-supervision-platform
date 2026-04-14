package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 供应商主数据
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_supplier")
public class SupplySupplier extends TenantEntity {

    @TableId(value = "id")
    private Long id;

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

    private Date onboardTime;

    private Date offboardTime;

    private String remark;

    @TableLogic
    private String delFlag;
}
