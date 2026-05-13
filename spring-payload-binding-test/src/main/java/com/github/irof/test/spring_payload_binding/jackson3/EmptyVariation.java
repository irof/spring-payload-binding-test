package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.PayloadTypeUtils;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.introspect.ClassIntrospector;
import tools.jackson.databind.node.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 空/ゼロ値を埋めるバリエーションです。
 * String→"", コレクション→[], primitive→デフォルト値 (0/false)、
 * ネスト object は再帰的に空フィールドを持つ object、@JsonValue 型は値型を辿った先の empty を生成します。
 */
public final class EmptyVariation implements Jackson3Variation {

    /**
     * コンストラクタ
     */
    public EmptyVariation() {
    }

    @Override
    public String name() {
        return "empty";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper mapper) {
        ClassIntrospector ci = mapper.serializationConfig().classIntrospectorInstance();
        return build(type, mapper, new HashSet<>(), ci);
    }

    private JsonNode build(JavaType type, ObjectMapper mapper, Set<JavaType> path, ClassIntrospector ci) {
        if (type.isReferenceType()) return build(type.getReferencedType(), mapper, path, ci);
        Class<?> raw = type.getRawClass();
        JsonNode scalar = emptyScalar(raw);
        if (scalar != null) return scalar;
        if (type.isArrayType() || type.isCollectionLikeType()) return mapper.createArrayNode();
        if (type.isMapLikeType()) return mapper.createObjectNode();
        if (PayloadTypeUtils.isFrameworkType(raw)) return NullNode.instance;
        if (!path.add(type)) return NullNode.instance;
        try {
            var desc = ci.introspectForSerialization(type, ci.introspectClassAnnotations(type));
            AnnotatedMember jsonValue = desc.findJsonValueAccessor();
            if (jsonValue != null) {
                return build(jsonValue.getType(), mapper, path, ci);
            }
            ObjectNode obj = mapper.createObjectNode();
            for (BeanPropertyDefinition prop : desc.findProperties()) {
                obj.set(prop.getName(), build(prop.getPrimaryType(), mapper, path, ci));
            }
            return obj;
        } finally {
            path.remove(type);
        }
    }

    private static JsonNode emptyScalar(Class<?> raw) {
        if (raw == String.class || raw == CharSequence.class) return StringNode.valueOf("");
        if (raw == char.class || raw == Character.class) return StringNode.valueOf("");
        if (raw == boolean.class || raw == Boolean.class) return BooleanNode.FALSE;
        if (raw.isPrimitive() || Number.class.isAssignableFrom(raw)) return IntNode.valueOf(0);
        if (raw.isEnum()) return NullNode.instance;
        return null;
    }
}
