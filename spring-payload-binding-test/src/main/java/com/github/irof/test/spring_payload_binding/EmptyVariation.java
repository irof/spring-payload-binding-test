package com.github.irof.test.spring_payload_binding;

/**
 * 空/ゼロ値を埋めるビルトインバリエーションです。
 * {@link EngineVariation#EMPTY} として提供されます。
 */
public record EmptyVariation() implements EngineVariation {

    @Override
    public String name() {
        return "empty";
    }
}
