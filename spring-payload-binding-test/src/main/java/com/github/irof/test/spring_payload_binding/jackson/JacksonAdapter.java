package com.github.irof.test.spring_payload_binding.jackson;

import com.github.irof.test.spring_payload_binding.TypeConfigurer;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Jackson バージョン固有の API を抽象化するアダプターインタフェースです。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 * @param <N> JsonNode（Jacksonのノード型）
 */
public interface JacksonAdapter<T, N> {

    // --- 型クエリ ---

    boolean isReferenceType(T type);

    T referencedType(T type);

    boolean isCollectionLike(T type);

    T contentType(T type);

    boolean isMapLike(T type);

    T keyType(T type);

    Class<?> rawClass(T type);

    /**
     * コレクション・配列・マップを含む広義のコンテナ型かどうかを返します。
     */
    boolean isContainerType(T type);

    /**
     * Java の {@link Type} から T を構築します。
     */
    T constructType(Type type);

    /**
     * Nth 型パラメータを返します。存在しない場合は不明型を返します。Optional/HttpEntity のアンラップに使用します。
     */
    T containedTypeOrUnknown(T type, int index);

    /**
     * 型の正規文字列表現（例: {@code java.util.List<java.lang.String>}）を返します。
     */
    String toCanonical(T type);

    // --- イントロスペクション ---

    /**
     * {@code @JsonValue} アノテーションが付いたアクセサーの型を返します。存在しない場合は空を返します。
     */
    Optional<T> findJsonValueType(T type);

    /**
     * シリアライズ対象のプロパティ一覧を返します。
     */
    List<PropertyDef<T>> findProperties(T type);

    record PropertyDef<P>(String name, P type) {
    }

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

    /**
     * Map キーなどのテキスト表現を返します。
     */
    String nodeToText(N node);

    /**
     * {@link TypeConfigurer} の値（String/Integer/Long/Boolean）を N に変換します。
     */
    N primitiveToNode(Object value);

    // --- JSON 読み書き ---

    /**
     * オブジェクトを JSON 文字列にシリアライズします。
     */
    String writeValueAsString(Object value) throws Exception;

    /**
     * JSON 文字列を N にパースします。
     */
    N readTree(String json) throws Exception;

    /**
     * JSON ファイルを N にパースします。
     */
    N readTree(File file) throws Exception;

    /**
     * JSON 文字列を型 T に従ってデシリアライズします。
     */
    Object readValue(String json, T type) throws Exception;

    /**
     * N をインデント付き JSON としてファイルに書き出します。
     */
    void writePrettyValue(File file, N value) throws Exception;

    /**
     * N をインデント付き JSON 文字列に変換します（ログ用）。
     */
    String toPrettyString(N node);
}
