package com.github.irof.test.spring_payload_binding;

import java.util.Map;

/**
 * 型別カスタム値を持つバリエーションです。
 * {@link Variation#customMapping} で生成されます。
 */
public interface CustomMappingVariation extends Variation {

    /**
     * カスタム値のマッピングを返します。
     * 値は {@code String}、{@code Integer}、{@code Long}、{@code Boolean} のいずれかです。
     *
     * @return 型からカスタム値へのマッピング
     */
    Map<Class<?>, Object> customValues();
}
