package com.csmp.supply.support;

import com.csmp.common.core.utils.StringUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 兼容请求中 JSON 对象或 JSON 字符串两种写法，统一转换为字符串存储。
 *
 * @author csmp
 */
public class JsonObjectStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.readValueAsTree();
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String value = node.textValue();
            return StringUtils.isBlank(value) ? null : value;
        }
        ObjectMapper objectMapper = (ObjectMapper) parser.getCodec();
        return objectMapper.writeValueAsString(node);
    }
}
