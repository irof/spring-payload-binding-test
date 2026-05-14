package com.github.irof.test.spring_payload_binding;

import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.*;

/**
 * バリエーション別の JSON 構築ロジックを提供するエンジンクラスです。
 * {@link VariationAdapter} を通じて Jackson バージョン固有の API を抽象化することで、
 * 全バリエーションのロジックをここに一元化しています。
 *
 * @param <T> JavaType（Jackson バージョン固有の型表現）
 * @param <N> JsonNode（Jackson バージョン固有のノード型）
 */
public final class VariationEngine<T, N> {

    private final VariationAdapter<T, N> adapter;

    public VariationEngine(VariationAdapter<T, N> adapter) {
        this.adapter = adapter;
    }

    /**
     * 全フィールドにサンプル値を埋めた JSON を構築します。
     */
    public N buildSample(T type, Map<Class<?>, N> customValues) {
        return buildSampleInternal(type, new HashSet<>(), customValues);
    }

    private N buildSampleInternal(T type, Set<T> path, Map<Class<?>, N> customValues) {
        if (adapter.isReferenceType(type)) {
            return buildSampleInternal(adapter.referencedType(type), path, customValues);
        }
        Class<?> raw = adapter.rawClass(type);
        N custom = customValues.get(raw);
        if (custom != null) return custom;
        N scalar = sampleScalar(raw);
        if (scalar != null) return scalar;
        if (adapter.isCollectionLike(type)) {
            return adapter.arrayNode(List.of(buildSampleInternal(adapter.contentType(type), path, customValues)));
        }
        if (adapter.isMapLike(type)) {
            N keyNode = buildSampleInternal(adapter.keyType(type), path, customValues);
            N valueNode = buildSampleInternal(adapter.contentType(type), path, customValues);
            Map<String, N> fields = new LinkedHashMap<>();
            fields.put(adapter.nodeToText(keyNode), valueNode);
            return adapter.objectNode(fields);
        }
        if (PayloadTypeUtils.isFrameworkType(raw)) return adapter.nullNode();
        if (!path.add(type)) return adapter.nullNode();
        try {
            Optional<T> jv = adapter.findJsonValueType(type);
            if (jv.isPresent()) return buildSampleInternal(jv.get(), path, customValues);
            Map<String, N> fields = new LinkedHashMap<>();
            for (var prop : adapter.findProperties(type)) {
                fields.put(prop.name(), buildSampleInternal(prop.type(), path, customValues));
            }
            return adapter.objectNode(fields);
        } finally {
            path.remove(type);
        }
    }

    private N sampleScalar(Class<?> raw) {
        if (raw == String.class || raw == CharSequence.class) return adapter.stringNode("sample");
        if (raw == char.class || raw == Character.class) return adapter.stringNode("x");
        if (raw == boolean.class || raw == Boolean.class) return adapter.boolNode(true);
        if (raw == int.class || raw == Integer.class) return adapter.intNode(1);
        if (raw == long.class || raw == Long.class) return adapter.longNode(1L);
        if (raw == short.class || raw == Short.class) return adapter.intNode(1);
        if (raw == byte.class || raw == Byte.class) return adapter.intNode(1);
        if (raw == float.class || raw == Float.class) return adapter.floatNode(1.0f);
        if (raw == double.class || raw == Double.class) return adapter.doubleNode(1.0);
        if (Number.class.isAssignableFrom(raw)) return adapter.intNode(1);
        if (raw.isEnum()) {
            Object[] consts = raw.getEnumConstants();
            return consts.length > 0 ? adapter.stringNode(consts[0].toString()) : adapter.nullNode();
        }
        if (raw == UUID.class) return adapter.stringNode("00000000-0000-0000-0000-000000000000");
        if (raw == LocalDate.class) return adapter.stringNode("2024-01-01");
        if (raw == LocalDateTime.class) return adapter.stringNode("2024-01-01T00:00:00");
        if (raw == Instant.class || raw == OffsetDateTime.class || raw == ZonedDateTime.class)
            return adapter.stringNode("2024-01-01T00:00:00Z");
        if (raw == URI.class || raw == URL.class) return adapter.stringNode("https://example.com/");
        return null;
    }

    /**
     * 全フィールドを null にした JSON を構築します。
     * {@code @JsonValue} 型の場合は top-level null を返します。
     */
    public N buildNull(T type, Map<Class<?>, N> customValues) {
        N custom = customValues.get(adapter.rawClass(type));
        if (custom != null) return custom;
        Optional<T> jv = adapter.findJsonValueType(type);
        if (jv.isPresent()) return adapter.nullNode();
        Map<String, N> fields = new LinkedHashMap<>();
        for (var prop : adapter.findProperties(type)) {
            N propCustom = customValues.get(adapter.rawClass(prop.type()));
            fields.put(prop.name(), propCustom != null ? propCustom : adapter.nullNode());
        }
        return adapter.objectNode(fields);
    }

    /**
     * 空/ゼロ値を埋めた JSON を構築します。
     * String→"", コレクション→[], primitive→デフォルト値 (0/false)
     */
    public N buildEmpty(T type, Map<Class<?>, N> customValues) {
        return buildEmptyInternal(type, new HashSet<>(), customValues);
    }

    private N buildEmptyInternal(T type, Set<T> path, Map<Class<?>, N> customValues) {
        if (adapter.isReferenceType(type)) {
            return buildEmptyInternal(adapter.referencedType(type), path, customValues);
        }
        Class<?> raw = adapter.rawClass(type);
        N custom = customValues.get(raw);
        if (custom != null) return custom;
        N scalar = emptyScalar(raw);
        if (scalar != null) return scalar;
        if (adapter.isCollectionLike(type)) return adapter.arrayNode(List.of());
        if (adapter.isMapLike(type)) return adapter.objectNode(Map.of());
        if (PayloadTypeUtils.isFrameworkType(raw)) return adapter.nullNode();
        if (!path.add(type)) return adapter.nullNode();
        try {
            Optional<T> jv = adapter.findJsonValueType(type);
            if (jv.isPresent()) return buildEmptyInternal(jv.get(), path, customValues);
            Map<String, N> fields = new LinkedHashMap<>();
            for (var prop : adapter.findProperties(type)) {
                fields.put(prop.name(), buildEmptyInternal(prop.type(), path, customValues));
            }
            return adapter.objectNode(fields);
        } finally {
            path.remove(type);
        }
    }

    private N emptyScalar(Class<?> raw) {
        if (raw == String.class || raw == CharSequence.class) return adapter.stringNode("");
        if (raw == char.class || raw == Character.class) return adapter.stringNode("");
        if (raw == boolean.class || raw == Boolean.class) return adapter.boolNode(false);
        if (raw.isPrimitive() || Number.class.isAssignableFrom(raw)) return adapter.intNode(0);
        if (raw.isEnum()) return adapter.nullNode();
        return null;
    }
}
