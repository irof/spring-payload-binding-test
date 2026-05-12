package com.example.testtool;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@SpringBootTest
public abstract class JsonBindingContractTestBase {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestMappingHandlerMapping handlerMapping;

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        return EndpointPayloadTypes.collect(handlerMapping, objectMapper).stream()
                .map(type -> DynamicTest.dynamicTest(type.toCanonical(), () -> verify(type)))
                .toList();
    }

    private void verify(JavaType type) throws Exception {
        Object instance = objectMapper.readValue("{}", type);
        String json = objectMapper.writeValueAsString(instance);
        objectMapper.readValue(json, type);
    }
}
