package com.csmp.supply.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采集执行模式
 *
 * @author csmp
 */
@Getter
@AllArgsConstructor
public enum CollectExecuteModeEnum {

    MANUAL("manual"),
    SCHEDULED("scheduled"),
    REFRESH("refresh");

    private final String code;
}
