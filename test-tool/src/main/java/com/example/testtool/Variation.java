package com.example.testtool;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを生成する。
 * 利用側で実装すれば任意のバリエーション (例: "edge-case", "minimum") を追加できる。
 */
public interface Variation {

    /** Filename にも使われるバリエーション名。同じ payload 型内でユニーク。 */
    String name();

    /** バリエーションに応じた JSON を構築する。 */
    JsonNode build(JavaType type, ObjectMapper objectMapper);

    /** 全フィールドにサンプル値を埋めるビルトインバリエーション。 */
    Variation SAMPLE = new SampleVariation();

    /** 全フィールド null (`@JsonValue` 型は top-level null) のビルトインバリエーション。 */
    Variation NULL = new NullVariation();
}
