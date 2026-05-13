package com.github.irof.test.spring_payload_binding.jackson3;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 全フィールドにサンプル値を埋めるバリエーションです。
 */
public final class SampleVariation implements Jackson3Variation {

    @Override
    public String name() {
        return "sample";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper mapper) {
        return new SampleJsonFactory(mapper).build(type);
    }
}
