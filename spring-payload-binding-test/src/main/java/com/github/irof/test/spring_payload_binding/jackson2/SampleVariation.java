package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.*;
import com.github.irof.test.spring_payload_binding.PayloadTypeUtils;

import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 全フィールドにサンプル値を埋めるバリエーションです。
 */
public final class SampleVariation implements Jackson2Variation {

    private final Map<Class<?>, JsonNode> customValues;

    /**
     * カスタムサンプル値なしで動作するコンストラクタ。
     */
    public SampleVariation() {
        this(Map.of());
    }

    /**
     * 型ごとのカスタムサンプル値を指定するコンストラクタ。
     * 指定した型に対してはカスタム値が組み込みのデフォルト値より優先されます。
     *
     * @param customValues 型からサンプル値へのマッピング
     */
    public SampleVariation(Map<Class<?>, JsonNode> customValues) {
        this.customValues = Map.copyOf(customValues);
    }

    @Override
    public String name() {
        return "sample";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper objectMapper) {
        return buildInternal(objectMapper, type, new HashSet<>());
    }

    private JsonNode buildInternal(ObjectMapper mapper, JavaType type, Set<JavaType> path) {
        if (type.isReferenceType()) {
            return buildInternal(mapper, type.getReferencedType(), path);
        }
        Class<?> raw = type.getRawClass();
        JsonNode custom = customValues.get(raw);
        if (custom != null) return custom;
        JsonNode scalar = scalarSample(raw);
        if (scalar != null) return scalar;

        if (type.isArrayType() || type.isCollectionLikeType()) {
            ArrayNode arr = mapper.createArrayNode();
            arr.add(buildInternal(mapper, type.getContentType(), path));
            return arr;
        }
        if (type.isMapLikeType()) {
            ObjectNode obj = mapper.createObjectNode();
            JsonNode keyNode = buildInternal(mapper, type.getKeyType(), path);
            obj.set(keyNode.asText(), buildInternal(mapper, type.getContentType(), path));
            return obj;
        }
        if (PayloadTypeUtils.isFrameworkType(raw)) {
            return NullNode.instance;
        }
        if (!path.add(type)) {
            return NullNode.instance;
        }
        try {
            BeanDescription desc = mapper.getSerializationConfig().introspect(type);
            AnnotatedMember jsonValue = desc.findJsonValueAccessor();
            if (jsonValue != null) {
                return buildInternal(mapper, mapper.constructType(jsonValue.getType()), path);
            }
            ObjectNode obj = mapper.createObjectNode();
            for (BeanPropertyDefinition prop : desc.findProperties()) {
                obj.set(prop.getName(), buildInternal(mapper, prop.getPrimaryType(), path));
            }
            return obj;
        } finally {
            path.remove(type);
        }
    }

    private static JsonNode scalarSample(Class<?> raw) {
        if (raw == String.class || raw == CharSequence.class) return TextNode.valueOf("sample");
        if (raw == char.class || raw == Character.class) return TextNode.valueOf("x");
        if (raw == boolean.class || raw == Boolean.class) return BooleanNode.TRUE;
        if (raw == int.class || raw == Integer.class) return IntNode.valueOf(1);
        if (raw == long.class || raw == Long.class) return LongNode.valueOf(1L);
        if (raw == short.class || raw == Short.class) return ShortNode.valueOf((short) 1);
        if (raw == byte.class || raw == Byte.class) return IntNode.valueOf(1);
        if (raw == float.class || raw == Float.class) return FloatNode.valueOf(1.0f);
        if (raw == double.class || raw == Double.class) return DoubleNode.valueOf(1.0);
        if (Number.class.isAssignableFrom(raw)) return IntNode.valueOf(1);
        if (raw.isEnum()) {
            Object[] consts = raw.getEnumConstants();
            return consts.length > 0 ? TextNode.valueOf(consts[0].toString()) : NullNode.instance;
        }
        if (raw == UUID.class) return TextNode.valueOf("00000000-0000-0000-0000-000000000000");
        if (raw == LocalDate.class) return TextNode.valueOf("2024-01-01");
        if (raw == LocalDateTime.class) return TextNode.valueOf("2024-01-01T00:00:00");
        if (raw == Instant.class || raw == OffsetDateTime.class || raw == ZonedDateTime.class)
            return TextNode.valueOf("2024-01-01T00:00:00Z");
        if (raw == URI.class || raw == URL.class) return TextNode.valueOf("https://example.com/");
        return null;
    }
}
