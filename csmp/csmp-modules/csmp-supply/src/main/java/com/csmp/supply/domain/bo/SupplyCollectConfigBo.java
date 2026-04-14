package com.csmp.supply.domain.bo;

import com.csmp.common.core.validate.AddGroup;
import com.csmp.common.core.validate.EditGroup;
import com.csmp.common.json.validate.JsonPattern;
import com.csmp.common.json.validate.JsonType;
import com.csmp.supply.support.JsonObjectStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采集配置请求对象
 *
 * @author csmp
 */
@Data
public class SupplyCollectConfigBo {

    @NotNull(message = "采集配置ID不能为空", groups = EditGroup.class)
    private Long collectConfigId;
    @NotNull(message = "云平台ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long cloudPlatformId;
    @NotBlank(message = "提供商编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String providerCode;
    @NotBlank(message = "采集地址不能为空", groups = {AddGroup.class, EditGroup.class})
    private String collectUrl;
    private String syncEndpoint;
    @NotBlank(message = "采集范围不能为空", groups = {AddGroup.class, EditGroup.class})
    private String collectScope;
    @NotBlank(message = "采集模式不能为空", groups = {AddGroup.class, EditGroup.class})
    private String collectMode;
    @NotBlank(message = "同步策略不能为空", groups = {AddGroup.class, EditGroup.class})
    private String syncStrategy;
    @NotBlank(message = "连接器编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String connectorCode;
    @NotBlank(message = "鉴权类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String authType;
    @JsonDeserialize(using = JsonObjectStringDeserializer.class)
    @JsonPattern(type = JsonType.OBJECT, message = "鉴权配置必须为JSON对象")
    private String authPayload;
    @JsonDeserialize(using = JsonObjectStringDeserializer.class)
    @JsonPattern(type = JsonType.OBJECT, message = "范围过滤必须为JSON对象")
    private String scopeFilter;
    @JsonDeserialize(using = JsonObjectStringDeserializer.class)
    @JsonPattern(type = JsonType.OBJECT, message = "采集扩展必须为JSON对象")
    private String collectOptions;
    @NotBlank(message = "执行周期不能为空", groups = {AddGroup.class, EditGroup.class})
    private String executeCycle;
    @Min(value = 1, message = "超时时间必须大于0")
    private Integer timeoutSeconds;
    @Min(value = 0, message = "重试次数不能小于0")
    private Integer retryTimes;
    private Boolean verifySsl;
    private String status;
    private String remark;
}
