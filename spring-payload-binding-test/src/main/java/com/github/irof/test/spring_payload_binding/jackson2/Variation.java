package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを生成するインタフェースです。
 * 利用側で実装すれば任意のバリエーション (例: "edge-case", "minimum") を追加できます。
 */
public interface Variation {

    /**
     * バリエーション名を返します。
     * Filename にも使われ、同じ payload 型内でユニークである必要があります。
     *
     * @return バリエーション名
     */
    String name();

    /**
     * バリエーションに応じた JSON を構築します。
     *
     * @param type         構築する型
     * @param objectMapper 使用する ObjectMapper
     * @return 構築された JsonNode
     */
    JsonNode build(JavaType type, ObjectMapper objectMapper);

    /**
     * 全フィールドにサンプル値を埋めるビルトインバリエーションです。
     */
    Variation SAMPLE = new SampleVariation();

    /**
     * 全フィールド null ({@code @JsonValue} 型は top-level null) のビルトインバリエーションです。
     */
    Variation NULL = new NullVariation();

    /**
     * 空/ゼロ値 (String→"", コレクション→[], primitive→デフォルト値) のビルトインバリエーションです。
     */
    Variation EMPTY = new EmptyVariation();
}
