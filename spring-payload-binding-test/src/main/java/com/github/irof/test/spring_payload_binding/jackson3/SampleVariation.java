package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.PayloadTypeUtils;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.introspect.ClassIntrospector;
import tools.jackson.databind.node.*;

import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 全フィールドにサンプル値を埋めるバリエーションです。
 */
public final class SampleVariation implements Jackson3Variation {

    @Override
    public String name() {
        return "sample";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper mapper) {
        ClassIntrospector ci = mapper.serializationConfig().classIntrospectorInstance();
        return buildInternal(type, new HashSet<>(), ci, mapper);
    }

    private JsonNode buildInternal(JavaType type, Set<JavaType> path, ClassIntrospector ci, ObjectMapper mapper) {
        if (type.isReferenceType()) {
            return buildInternal(type.getReferencedType(), path, ci, mapper);
        }
        Class<?> raw = type.getRawClass();
        JsonNode scalar = scalarSample(raw);
        if (scalar != null) return scalar;

        if (type.isArrayType() || type.isCollectionLikeType()) {
            ArrayNode arr = mapper.createArrayNode();
            arr.add(buildInternal(type.getContentType(), path, ci, mapper));
            return arr;
        }
        if (type.isMapLikeType()) {
            ObjectNode obj = mapper.createObjectNode();
            JsonNode keyNode = buildInternal(type.getKeyType(), path, ci, mapper);
            obj.set(keyNode.asText(), buildInternal(type.getContentType(), path, ci, mapper));
            return obj;
        }
        if (PayloadTypeUtils.isFrameworkType(raw)) {
            return NullNode.instance;
        }
        if (!path.add(type)) {
            return NullNode.instance;
        }
        try {
            var desc = ci.introspectForSerialization(type, ci.introspectClassAnnotations(type));
            AnnotatedMember jsonValue = desc.findJsonValueAccessor();
            if (jsonValue != null) {
                return buildInternal(jsonValue.getType(), path, ci, mapper);
            }
            ObjectNode obj = mapper.createObjectNode();
            for (BeanPropertyDefinition prop : desc.findProperties()) {
                obj.set(prop.getName(), buildInternal(prop.getPrimaryType(), path, ci, mapper));
            }
            return obj;
        } finally {
            path.remove(type);
        }
    }

    private static JsonNode scalarSample(Class<?> raw) {
        if (raw == String.class || raw == CharSequence.class) return StringNode.valueOf("sample");
        if (raw == char.class || raw == Character.class) return StringNode.valueOf("x");
        if (raw == boolean.class || raw == Boolean.class) return BooleanNode.TRUE;
        if (raw == int.class || raw == Integer.class) return IntNode.valueOf(1);
        if (raw == long.class || raw == Long.class) return LongNode.valueOf(1L);
        if (raw == short.class || raw == Short.class) return IntNode.valueOf(1);
        if (raw == byte.class || raw == Byte.class) return IntNode.valueOf(1);
        if (raw == float.class || raw == Float.class) return FloatNode.valueOf(1.0f);
        if (raw == double.class || raw == Double.class) return DoubleNode.valueOf(1.0);
        if (Number.class.isAssignableFrom(raw)) return IntNode.valueOf(1);
        if (raw.isEnum()) {
            Object[] consts = raw.getEnumConstants();
            return consts.length > 0 ? StringNode.valueOf(consts[0].toString()) : NullNode.instance;
        }
        if (raw == UUID.class) return StringNode.valueOf("00000000-0000-0000-0000-000000000000");
        if (raw == LocalDate.class) return StringNode.valueOf("2024-01-01");
        if (raw == LocalDateTime.class) return StringNode.valueOf("2024-01-01T00:00:00");
        if (raw == Instant.class || raw == OffsetDateTime.class || raw == ZonedDateTime.class)
            return StringNode.valueOf("2024-01-01T00:00:00Z");
        if (raw == URI.class || raw == URL.class) return StringNode.valueOf("https://example.com/");
        return null;
    }
}
