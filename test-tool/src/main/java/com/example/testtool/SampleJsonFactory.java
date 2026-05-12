package com.example.testtool;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SampleJsonFactory {

    private final ObjectMapper mapper;

    public SampleJsonFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode build(JavaType type) {
        return build(type, new HashSet<>());
    }

    private JsonNode build(JavaType type, Set<JavaType> path) {
        if (type.isReferenceType()) {
            return build(type.getReferencedType(), path);
        }
        Class<?> raw = type.getRawClass();
        JsonNode scalar = scalarSample(raw);
        if (scalar != null) return scalar;

        if (type.isArrayType() || type.isCollectionLikeType()) {
            ArrayNode arr = mapper.createArrayNode();
            arr.add(build(type.getContentType(), path));
            return arr;
        }
        if (type.isMapLikeType()) {
            ObjectNode obj = mapper.createObjectNode();
            obj.set("key", build(type.getContentType(), path));
            return obj;
        }
        if (raw.getName().startsWith("java.")) {
            return NullNode.instance;
        }
        if (!path.add(type)) {
            return NullNode.instance;
        }
        try {
            ObjectNode obj = mapper.createObjectNode();
            BeanDescription desc = mapper.getSerializationConfig().introspect(type);
            for (BeanPropertyDefinition prop : desc.findProperties()) {
                obj.set(prop.getName(), build(prop.getPrimaryType(), path));
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
