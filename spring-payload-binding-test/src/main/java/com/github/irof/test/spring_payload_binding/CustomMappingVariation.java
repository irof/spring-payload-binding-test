package com.github.irof.test.spring_payload_binding;

import java.util.Map;

/**
 * 型別カスタム値を持つバリエーションです。
 * {@link Variation#customMapping} で生成されます。
 */
public record CustomMappingVariation(Variation base, Map<Class<?>, Object> customValues) implements Variation {

    @Override
    public String name() {
        return base.name();
    }
}
