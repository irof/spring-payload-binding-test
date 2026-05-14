package com.github.irof.test.spring_payload_binding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Jackson バージョン固有の API を抽象化するアダプターインタフェースです。
 *
 * <ul>
 *   <li>{@code T} = JavaType（Jackson2/3 の型表現）</li>
 *   <li>{@code N} = JsonNode（Jackson2/3 のノード型）</li>
 * </ul>
 */
public interface VariationAdapter<T, N> {

    // --- 型クエリ ---

    boolean isReferenceType(T type);

    T referencedType(T type);

    boolean isCollectionLike(T type);

    T contentType(T type);

    boolean isMapLike(T type);

    T keyType(T type);

    Class<?> rawClass(T type);

    // --- イントロスペクション ---

    /**
     * {@code @JsonValue} アノテーションが付いたアクセサーの型を返します。存在しない場合は空を返します。
     */
    Optional<T> findJsonValueType(T type);

    /**
     * シリアライズ対象のプロパティ一覧を返します。
     */
    List<PropertyDef<T>> findProperties(T type);

    record PropertyDef<P>(String name, P type) {}

    // --- ノード生成 ---

    N nullNode();

    N stringNode(String value);

    N intNode(int value);

    N longNode(long value);

    N boolNode(boolean value);

    N floatNode(float value);

    N doubleNode(double value);

    N objectNode(Map<String, N> fields);

    N arrayNode(List<N> elements);

    /** Map キーなどのテキスト表現を返します。 */
    String nodeToText(N node);

    /** {@link TypeMapping} の値（String/Integer/Long/Boolean）を N に変換します。 */
    N primitiveToNode(Object value);
}
