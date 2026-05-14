package com.github.irof.test.spring_payload_binding;

/**
 * 全フィールドにサンプル値を埋めるビルトインバリエーションです。
 * {@link EngineVariation#SAMPLE} として提供されます。
 */
public record SampleVariation() implements EngineVariation {

    @Override
    public String name() {
        return "sample";
    }
}
