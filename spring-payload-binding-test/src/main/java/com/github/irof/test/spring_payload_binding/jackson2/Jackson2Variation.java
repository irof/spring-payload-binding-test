package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irof.test.spring_payload_binding.Variation;

import java.util.Map;

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
     * 型別カスタム値を適用した JSON を構築します。
     * デフォルト実装はトップレベルの型チェックのみ行います。
     * 再帰的な構築が必要な場合は各実装クラスでオーバーライドしてください。
     *
     * @param type         構築する型
     * @param mapper       使用する ObjectMapper
     * @param customValues 型からカスタム値へのマッピング
     * @return 構築された JsonNode
     */
    default JsonNode buildWithCustomValues(JavaType type, ObjectMapper mapper, Map<Class<?>, JsonNode> customValues) {
        JsonNode custom = customValues.get(type.getRawClass());
        return custom != null ? custom : build(type, mapper);
    }

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
