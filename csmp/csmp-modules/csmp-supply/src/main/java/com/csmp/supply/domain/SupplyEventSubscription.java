package com.csmp.supply.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csmp.common.encrypt.annotation.EncryptField;
import com.csmp.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 实时事件订阅
 *
 * @author csmp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_event_subscription")
public class SupplyEventSubscription extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long cloudPlatformId;

    private String providerCode;

    private String eventScope;

    private String ingestMode;

    private String topicName;

    private String consumerGroup;

    private String endpointPath;

    private String authType;

    @EncryptField
    private String authPayload;

    private String dataFormat;

    private String schemaVersion;

    private String status;

    private Date lastEventTime;

    private String lastErrorMessage;

    private String remark;

    @TableLogic
    private String delFlag;
}
