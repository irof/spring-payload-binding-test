package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.Variation;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Jackson3 を使用したバリエーションのインタフェースです。
 * 利用側で実装すれば任意のバリエーションを追加できます。
 */
public interface Jackson3Variation extends Variation {

    /**
     * バリエーションに応じた JSON を構築します。
     *
     * @param type   構築する型
     * @param mapper 使用する ObjectMapper
     * @return 構築された JsonNode
     */
    JsonNode build(JavaType type, ObjectMapper mapper);

    /**
     * 全フィールドにサンプル値を埋めるビルトインバリエーションです。
     */
    Jackson3Variation SAMPLE = new SampleVariation();

    /**
     * 全フィールド null ({@code @JsonValue} 型は top-level null) のビルトインバリエーションです。
     */
    Jackson3Variation NULL = new NullVariation();

    /**
     * 空/ゼロ値 (String→"", コレクション→[], primitive→デフォルト値) のビルトインバリエーションです。
     */
    Jackson3Variation EMPTY = new EmptyVariation();
}
