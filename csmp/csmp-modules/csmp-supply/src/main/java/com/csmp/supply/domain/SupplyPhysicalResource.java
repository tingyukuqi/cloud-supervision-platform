package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.tenant.core.TenantEntity;
import com.csmp.supply.support.PostgresJsonbStringTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 物理资源台账
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "supply_physical_resource", autoResultMap = true)
public class SupplyPhysicalResource extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String resourceCode;

    private Long supplierId;

    private String deviceName;

    private String deviceType;

    private String deviceModel;

    private String serialNumber;

    private String assetTag;

    private String resourceStatus;

    private String rackLocation;

    private String idcLocation;

    private String manageIp;

    private Date purchaseDate;

    private Date expireDate;

    @TableField(typeHandler = PostgresJsonbStringTypeHandler.class)
    private String specPayload;

    private String remark;

    @TableLogic
    private String delFlag;
}
