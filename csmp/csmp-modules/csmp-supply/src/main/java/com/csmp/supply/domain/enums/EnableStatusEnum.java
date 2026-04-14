package com.csmp.supply.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启停状态枚举
 *
 * @author csmp
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum {

    ENABLE("0"),
    DISABLE("1");

    private final String code;

    public static boolean isEnabled(String code) {
        return ENABLE.code.equals(code);
    }
}
