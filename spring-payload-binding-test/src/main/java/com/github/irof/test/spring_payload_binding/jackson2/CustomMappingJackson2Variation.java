package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

final class CustomMappingJackson2Variation implements Jackson2Variation {

    private final Jackson2Variation base;
    private final Map<Class<?>, JsonNode> customValues;

    CustomMappingJackson2Variation(Jackson2Variation base, Map<Class<?>, JsonNode> customValues) {
        this.base = base;
        this.customValues = customValues;
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper mapper) {
        return base.buildWithCustomValues(type, mapper, customValues);
    }
}
