package com.csmp.supply.domain.bo;

import lombok.Data;

/**
 * 事件日志查询对象
 *
 * @author csmp
 */
@Data
public class SupplyEventLogBo {

    private String processStatus;

    private String eventScope;
}
