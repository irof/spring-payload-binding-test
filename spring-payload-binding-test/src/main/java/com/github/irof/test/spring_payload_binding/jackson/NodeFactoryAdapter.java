package com.github.irof.test.spring_payload_binding.jackson;

import java.util.List;
import java.util.Map;

/**
 * Jackson の JsonNode ファクトリ操作を抽象化します。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 * @param <N> JsonNode（Jacksonのノード型）
 */
public interface NodeFactoryAdapter<T, N> {

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
     * プリミティブ値（String/Integer/Long/Boolean）を N に変換します。
     */
    N primitiveToNode(Object value);
}
