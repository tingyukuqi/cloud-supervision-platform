package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.encrypt.annotation.EncryptField;
import com.csmp.common.tenant.core.TenantEntity;
import com.csmp.supply.support.PostgresJsonbStringTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 快照采集配置
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "supply_collect_config", autoResultMap = true)
public class SupplyCollectConfig extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long cloudPlatformId;

    private String providerCode;

    private String collectUrl;

    private String syncEndpoint;

    private String collectScope;

    private String collectMode;

    private String syncStrategy;

    private String connectorCode;

    private String authType;

    @EncryptField
    private String authPayload;

    @TableField(typeHandler = PostgresJsonbStringTypeHandler.class)
    private String scopeFilter;

    @TableField(typeHandler = PostgresJsonbStringTypeHandler.class)
    private String collectOptions;

    private String executeCycle;

    private Integer timeoutSeconds;

    private Integer retryTimes;

    private Boolean verifySsl;

    private String status;

    private Date lastCollectTime;

    private Date lastSuccessTime;

    private String lastCollectStatus;

    private String lastErrorMessage;

    private Date nextCollectTime;

    private String remark;

    @TableLogic
    private String delFlag;
}
