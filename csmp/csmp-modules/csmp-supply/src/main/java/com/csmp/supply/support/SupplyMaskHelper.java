package com.csmp.supply.support;

import cn.hutool.core.lang.Dict;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.json.utils.JsonUtils;

import java.util.Map;

/**
 * 敏感字段掩码工具
 *
 * @author csmp
 */
public final class SupplyMaskHelper {

    private SupplyMaskHelper() {
    }

    public static Object maskJsonPayload(String payload) {
        Dict dict = JsonUtils.parseMap(payload);
        if (dict == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : dict.entrySet()) {
            if (entry.getValue() instanceof String value) {
                entry.setValue(mask(value));
            }
        }
        return dict;
    }

    public static String mask(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.length() <= 6) {
            return "***";
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
    }
}
