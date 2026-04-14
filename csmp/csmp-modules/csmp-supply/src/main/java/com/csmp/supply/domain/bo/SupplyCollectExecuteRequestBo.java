package com.csmp.supply.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 手工执行采集请求
 *
 * @author csmp
 */
@Data
public class SupplyCollectExecuteRequestBo {

    @NotBlank(message = "执行模式不能为空")
    private String executeMode;

    private String operatorRemark;
}
