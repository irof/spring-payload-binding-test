package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 空/ゼロ値を埋めるバリエーションです。
 * String→"", コレクション→[], primitive→デフォルト値 (0/false)、
 * ネスト object は再帰的に空フィールドを持つ object、@JsonValue 型は値型を辿った先の empty を生成します。
 */
final class EmptyVariation implements Variation {

    @Override
    public String name() {
        return "empty";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper objectMapper) {
        return build(type, objectMapper, new HashSet<>());
    }

    private JsonNode build(JavaType type, ObjectMapper objectMapper, Set<JavaType> path) {
        if (type.isReferenceType()) return build(type.getReferencedType(), objectMapper, path);
        Class<?> raw = type.getRawClass();
        JsonNode scalar = emptyScalar(raw);
        if (scalar != null) return scalar;
        if (type.isArrayType() || type.isCollectionLikeType()) return objectMapper.createArrayNode();
        if (type.isMapLikeType()) return objectMapper.createObjectNode();
        if (EndpointPayloadTypes.isFrameworkType(raw)) return NullNode.instance;
        if (!path.add(type)) return NullNode.instance;
        try {
            BeanDescription desc = objectMapper.getSerializationConfig().introspect(type);
            AnnotatedMember jsonValue = desc.findJsonValueAccessor();
            if (jsonValue != null) {
                return build(objectMapper.constructType(jsonValue.getType()), objectMapper, path);
            }
            ObjectNode obj = objectMapper.createObjectNode();
            for (BeanPropertyDefinition prop : desc.findProperties()) {
                obj.set(prop.getName(), build(prop.getPrimaryType(), objectMapper, path));
            }
            return obj;
        } finally {
            path.remove(type);
        }
    }

    private static JsonNode emptyScalar(Class<?> raw) {
        if (raw == String.class || raw == CharSequence.class) return TextNode.valueOf("");
        if (raw == char.class || raw == Character.class) return TextNode.valueOf("");
        if (raw == boolean.class || raw == Boolean.class) return BooleanNode.FALSE;
        if (raw.isPrimitive() || Number.class.isAssignableFrom(raw)) return IntNode.valueOf(0);
        if (raw.isEnum()) return NullNode.instance;
        return null;
    }
}
