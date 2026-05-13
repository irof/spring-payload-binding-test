package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irof.test.spring_payload_binding.Variation;

/**
 * Jackson2 を使用したバリエーションのインタフェースです。
 * 利用側で実装すれば任意のバリエーションを追加できます。
 */
public interface Jackson2Variation extends Variation {

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
    Jackson2Variation SAMPLE = new SampleVariation();

    /**
     * 全フィールド null ({@code @JsonValue} 型は top-level null) のビルトインバリエーションです。
     */
    Jackson2Variation NULL = new NullVariation();

    /**
     * 空/ゼロ値 (String→"", コレクション→[], primitive→デフォルト値) のビルトインバリエーションです。
     */
    Jackson2Variation EMPTY = new EmptyVariation();
}
