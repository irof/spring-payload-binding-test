package com.github.irof.test.spring_payload_binding;

/**
 * 全フィールドを null にするビルトインバリエーションです。
 * {@link EngineVariation#NULL} として提供されます。
 */
public record NullVariation() implements EngineVariation {

    @Override
    public String name() {
        return "null";
    }
}
