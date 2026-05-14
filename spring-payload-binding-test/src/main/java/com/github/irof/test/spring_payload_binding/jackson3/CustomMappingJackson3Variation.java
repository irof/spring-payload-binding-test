package com.github.irof.test.spring_payload_binding.jackson3;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

final class CustomMappingJackson3Variation implements Jackson3Variation {

    private final Jackson3Variation base;
    private final Map<Class<?>, JsonNode> customValues;

    CustomMappingJackson3Variation(Jackson3Variation base, Map<Class<?>, JsonNode> customValues) {
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
