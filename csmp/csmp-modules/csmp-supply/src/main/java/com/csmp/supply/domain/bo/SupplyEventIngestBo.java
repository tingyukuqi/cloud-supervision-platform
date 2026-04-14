package com.csmp.supply.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 实时事件接入对象
 *
 * @author csmp
 */
@Data
public class SupplyEventIngestBo {

    @NotBlank(message = "源事件ID不能为空")
    private String sourceEventId;

    private String eventScope;

    private String eventKey;

    private String eventTime;

    @NotNull(message = "事件载荷不能为空")
    private Map<String, Object> payload;
}
