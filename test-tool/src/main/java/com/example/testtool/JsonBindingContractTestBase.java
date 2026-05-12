package com.example.testtool;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public abstract class JsonBindingContractTestBase {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestMappingHandlerMapping handlerMapping;

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        SampleJsonFactory sampleFactory = new SampleJsonFactory(objectMapper);
        return EndpointPayloadTypes.collect(handlerMapping, objectMapper).stream()
                .map(type -> DynamicTest.dynamicTest(type.toCanonical(), () -> verify(type, sampleFactory)))
                .toList();
    }

    private void verify(JavaType type, SampleJsonFactory sampleFactory) throws Exception {
        JsonNode sample = sampleFactory.build(type);
        String json = objectMapper.writeValueAsString(sample);
        Object instance = objectMapper.readValue(json, type);
        JsonNode roundtripped = objectMapper.readTree(objectMapper.writeValueAsString(instance));
        assertEquals(sample, roundtripped, () -> "JSON round-trip mismatch for " + type.toCanonical());
    }
}
