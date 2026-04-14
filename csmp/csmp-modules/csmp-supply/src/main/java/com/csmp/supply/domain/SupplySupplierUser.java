package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商用户绑定
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_supplier_user")
public class SupplySupplierUser extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long supplierId;

    private Long userId;

    @TableLogic
    private String delFlag;
}
