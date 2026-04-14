package com.csmp.supply.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 状态变更请求
 *
 * @author csmp
 */
@Data
public class SupplyStatusBo {

    @NotBlank(message = "状态不能为空")
    private String status;
}
