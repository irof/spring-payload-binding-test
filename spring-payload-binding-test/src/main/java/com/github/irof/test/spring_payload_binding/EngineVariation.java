package com.github.irof.test.spring_payload_binding;

import java.util.function.Consumer;

/**
 * Jackson エンジンで JSON を生成するバリエーションです。
 * {@link #customMapping} で型別カスタム値を上書きできます。
 */
public interface EngineVariation extends Variation {

    /**
     * 型別カスタム値を持つバリエーションを生成します。
     * このバリエーションは呼び出し元の {@link #name()} を引き継ぎます。
     *
     * @param configurer 型ごとのカスタム値を設定するコンシューマ
     * @return カスタム値を持つバリエーション
     */
    default CustomMappingVariation customMapping(Consumer<TypeConfigurer> configurer) {
        TypeConfigurer mapping = new TypeConfigurer();
        configurer.accept(mapping);
        return new CustomMappingVariation(this, mapping.build());
    }

    /**
     * 全フィールドにサンプル値を埋めるビルトインバリエーションです。
     */
    EngineVariation SAMPLE = () -> "sample";

    /**
     * 全フィールドを null にするビルトインバリエーションです。
     */
    EngineVariation NULL = () -> "null";

    /**
     * 空/ゼロ値を埋めるビルトインバリエーションです。
     */
    EngineVariation EMPTY = () -> "empty";
}
