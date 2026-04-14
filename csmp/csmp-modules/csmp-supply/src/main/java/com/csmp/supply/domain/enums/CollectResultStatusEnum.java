package com.csmp.supply.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采集结果状态
 *
 * @author csmp
 */
@Getter
@AllArgsConstructor
public enum CollectResultStatusEnum {

    SUCCESS("success"),
    FAILED("failed"),
    RUNNING("running"),
    RECEIVED("received");

    private final String code;
}
