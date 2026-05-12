package com.example.testtool;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class SampleVariation implements Variation {

    @Override
    public String name() {
        return "sample";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper objectMapper) {
        return new SampleJsonFactory(objectMapper).build(type);
    }
}
