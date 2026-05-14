package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * 全フィールドを null にするバリエーションです。
 * {@code @JsonValue} 型の場合は top-level null を生成します。
 */
public final class NullVariation implements Jackson2Variation {

    /**
     * コンストラクタ
     */
    public NullVariation() {
    }

    @Override
    public String name() {
        return "null";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper objectMapper) {
        return buildWithCustomValues(type, objectMapper, Map.of());
    }

    @Override
    public JsonNode buildWithCustomValues(JavaType type, ObjectMapper mapper, Map<Class<?>, JsonNode> customValues) {
        JsonNode custom = customValues.get(type.getRawClass());
        if (custom != null) return custom;
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        if (desc.findJsonValueAccessor() != null) return NullNode.instance;
        ObjectNode obj = mapper.createObjectNode();
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            JsonNode propCustom = customValues.get(prop.getPrimaryType().getRawClass());
            obj.set(prop.getName(), propCustom != null ? propCustom : NullNode.instance);
        }
        return obj;
    }
}
