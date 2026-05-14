package com.github.irof.test.spring_payload_binding;

import java.util.Map;

/**
 * 型別カスタム値を持つバリエーションです。
 * {@link EngineVariation#customMapping} で生成されます。
 */
public record CustomMappingVariation(EngineVariation base, Map<Class<?>, Object> customValues)
        implements EngineVariation {

    @Override
    public String name() {
        return base.name();
    }
}
