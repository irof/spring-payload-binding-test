package com.github.irof.test.spring_payload_binding;

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
     * @param name       バリエーション名（ファイル名に使用）
     * @param configurer 型ごとのカスタム値を設定するコンシューマ
     * @return カスタム値を持つバリエーション
     */
    default CustomMappingVariation customMapping(String name, Consumer<TypeConfigurer> configurer) {
        TypeConfigurer mapping = new TypeConfigurer();
        configurer.accept(mapping);
        return new CustomMappingVariation(name, this, mapping.build());
    }
}
