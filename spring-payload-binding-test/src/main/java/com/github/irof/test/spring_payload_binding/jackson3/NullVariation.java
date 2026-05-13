package com.github.irof.test.spring_payload_binding.jackson3;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.introspect.ClassIntrospector;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 全フィールドを null にするバリエーションです。
 * {@code @JsonValue} 型の場合は top-level null を生成します。
 */
public final class NullVariation implements Jackson3Variation {

    @Override
    public String name() {
        return "null";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper mapper) {
        ClassIntrospector ci = mapper.serializationConfig().classIntrospectorInstance();
        var desc = ci.introspectForSerialization(type, ci.introspectClassAnnotations(type));
        if (desc.findJsonValueAccessor() != null) return NullNode.instance;
        ObjectNode obj = mapper.createObjectNode();
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            obj.putNull(prop.getName());
        }
        return obj;
    }
}
