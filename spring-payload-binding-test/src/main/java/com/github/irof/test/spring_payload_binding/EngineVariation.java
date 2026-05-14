package com.github.irof.test.spring_payload_binding;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Jackson エンジンで JSON を生成するバリエーションです。
 * {@link #customMapping} で型別カスタム値を上書きできます。
 */
public interface EngineVariation extends Variation {

    /**
     * 型別カスタム値を持つバリエーションを生成します。
     *
     * @param configurer 型ごとのカスタム値を設定するコンシューマ
     * @return カスタム値を持つバリエーション
     */
    default CustomMappingVariation customMapping(Consumer<TypeConfigurer> configurer) {
        return customMapping(name(), configurer);
    }

    /**
     * 名前を指定して型別カスタム値を持つバリエーションを生成します。
     *
     * @param name バリエーション名（ファイル名に使用）
     * @param configurer 型ごとのカスタム値を設定するコンシューマ
     * @return カスタム値を持つバリエーション
     */
    default CustomMappingVariation customMapping(String name, Consumer<TypeConfigurer> configurer) {
        TypeConfigurer mapping = new TypeConfigurer();
        configurer.accept(mapping);
        return new CustomMappingVariation(name, this, mapping.build());
    }

    /**
     * 名前だけを変えたバリエーションを生成します。
     * エンジンの動作（sample/null/empty）はこのバリエーションのものを引き継ぎます。
     *
     * @param name バリエーション名（ファイル名に使用）
     * @return 名前を変えたバリエーション
     */
    default EngineVariation withName(String name) {
        return new CustomMappingVariation(name, this, Map.of());
    }

    /**
     * 全フィールドにサンプル値を埋めるビルトインバリエーションです。
     */
    EngineVariation SAMPLE = new SampleVariation();

    /**
     * 全フィールドを null にするビルトインバリエーションです。
     */
    EngineVariation NULL = new NullVariation();

    /**
     * 空/ゼロ値を埋めるビルトインバリエーションです。
     */
    EngineVariation EMPTY = new EmptyVariation();
}
