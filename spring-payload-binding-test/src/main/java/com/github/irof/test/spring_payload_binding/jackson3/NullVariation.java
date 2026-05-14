package com.github.irof.test.spring_payload_binding.jackson3;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.introspect.ClassIntrospector;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * 全フィールドを null にするバリエーションです。
 * {@code @JsonValue} 型の場合は top-level null を生成します。
 */
public final class NullVariation implements Jackson3Variation {

    private final Map<Class<?>, JsonNode> customValues;

    /**
     * カスタム値なしで動作するコンストラクタ。
     */
    public NullVariation() {
        this(Map.of());
    }

    /**
     * 型ごとのカスタム値を指定するコンストラクタ。
     * 指定した型に対してはカスタム値が null より優先されます。
     *
     * @param customValues 型からカスタム値へのマッピング
     */
    public NullVariation(Map<Class<?>, JsonNode> customValues) {
        this.customValues = Map.copyOf(customValues);
    }

    @Override
    public String name() {
        return "null";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper objectMapper) {
        JsonNode custom = customValues.get(type.getRawClass());
        if (custom != null) return custom;
        ClassIntrospector ci = objectMapper.serializationConfig().classIntrospectorInstance();
        var desc = ci.introspectForSerialization(type, ci.introspectClassAnnotations(type));
        if (desc.findJsonValueAccessor() != null) return NullNode.instance;
        ObjectNode obj = objectMapper.createObjectNode();
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            JsonNode propCustom = customValues.get(prop.getPrimaryType().getRawClass());
            obj.set(prop.getName(), propCustom != null ? propCustom : NullNode.instance);
        }
        return obj;
    }
}
