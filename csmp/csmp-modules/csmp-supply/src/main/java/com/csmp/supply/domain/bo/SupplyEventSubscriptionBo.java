package com.csmp.supply.domain.bo;

import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.json.validate.JsonPattern;
import com.csmp.common.json.validate.JsonType;
import com.csmp.supply.support.JsonObjectStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 事件订阅请求对象
 *
 * @author csmp
 */
@Data
public class SupplyEventSubscriptionBo {

    @NotNull(message = "订阅ID不能为空", groups = EditGroup.class)
    private Long subscriptionId;
    @NotNull(message = "云平台ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long cloudPlatformId;
    @NotBlank(message = "提供商编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String providerCode;
    @NotBlank(message = "事件范围不能为空", groups = {AddGroup.class, EditGroup.class})
    private String eventScope;
    @NotBlank(message = "接入模式不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ingestMode;
    private String topicName;
    private String consumerGroup;
    private String endpointPath;
    private String authType;
    @JsonDeserialize(using = JsonObjectStringDeserializer.class)
    @JsonPattern(type = JsonType.OBJECT, message = "鉴权配置必须为JSON对象")
    private String authPayload;
    @NotBlank(message = "数据格式不能为空", groups = {AddGroup.class, EditGroup.class})
    private String dataFormat;
    @NotBlank(message = "模型版本不能为空", groups = {AddGroup.class, EditGroup.class})
    private String schemaVersion;
    private String status;
    private String remark;
}
