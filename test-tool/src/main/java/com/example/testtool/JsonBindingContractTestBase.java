package com.example.testtool;

import com.example.testtool.EndpointPayloadTypes.Direction;
import com.example.testtool.EndpointPayloadTypes.PayloadType;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class JsonBindingContractTestBase {

    public enum Mode { SAMPLE, WRITE, VERIFY }

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestMappingHandlerMapping handlerMapping;

    protected Mode mode() {
        return Mode.valueOf(System.getProperty("json.binding.mode", Mode.SAMPLE.name()).toUpperCase());
    }

    protected Path jsonDirectory() {
        return Path.of("src/test/resources/json-binding");
    }

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        Mode mode = mode();
        SampleJsonFactory sampleFactory = new SampleJsonFactory(objectMapper);
        return EndpointPayloadTypes.collect(handlerMapping, objectMapper).stream()
                .map(payload -> DynamicTest.dynamicTest(
                        "[" + mode + "][" + payload.direction() + "] " + payload.type().toCanonical(),
                        () -> run(payload, mode, sampleFactory)))
                .toList();
    }

    private void run(PayloadType payload, Mode mode, SampleJsonFactory sampleFactory) throws Exception {
        JsonNode source = (mode == Mode.VERIFY) ? loadFromFile(payload) : sampleFactory.build(payload.type());
        String sourceJson = objectMapper.writeValueAsString(source);

        if (payload.direction() == Direction.REQUEST) {
            Object instance = objectMapper.readValue(sourceJson, payload.type());
            JsonNode normalizedSource = objectMapper.readTree(sourceJson);
            verifyPopulated(payload.type(), instance, normalizedSource, payload.type().getRawClass().getSimpleName());
        } else {
            Object instance = objectMapper.readValue(sourceJson, payload.type());
            String serialized = objectMapper.writeValueAsString(instance);
            JsonNode normalizedSource = objectMapper.readTree(sourceJson);
            JsonNode actual = objectMapper.readTree(serialized);
            assertEquals(normalizedSource, actual,
                    () -> "serialized JSON differs from source for " + payload.type().toCanonical());
        }

        if (mode == Mode.WRITE) {
            writeToFile(payload, source);
        }
    }

    private JsonNode loadFromFile(PayloadType payload) throws Exception {
        Path file = fileFor(payload);
        if (!Files.exists(file)) {
            throw new AssertionError("missing JSON fixture: " + file.toAbsolutePath());
        }
        return objectMapper.readTree(file.toFile());
    }

    private void writeToFile(PayloadType payload, JsonNode source) throws Exception {
        Path file = fileFor(payload);
        Files.createDirectories(file.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), source);
    }

    /**
     * For each value the sample JSON populated, verify the deserialized instance
     * also has a non-null value at the same path. Skips value equality so custom
     * deserializers transforming the value don't trigger false positives.
     */
    private void verifyPopulated(JavaType type, Object instance, JsonNode expected, String path) throws Exception {
        if (instance == null) {
            throw new AssertionError(path + " was not deserialized (expected " + expected + ")");
        }
        if (type.isContainerType() && expected.isArray()) {
            Iterator<?> it = ((Iterable<?>) instance).iterator();
            int i = 0;
            for (JsonNode el : expected) {
                if (el.isNull()) { i++; continue; }
                if (!it.hasNext()) throw new AssertionError(path + "[" + i + "] missing");
                verifyPopulated(type.getContentType(), it.next(), el, path + "[" + i + "]");
                i++;
            }
            return;
        }
        Class<?> raw = type.getRawClass();
        if (raw.isPrimitive() || raw.isEnum() || raw.getName().startsWith("java.")) return;

        BeanDescription desc = objectMapper.getSerializationConfig().introspect(type);
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            JsonNode expectedValue = expected.get(prop.getName());
            if (expectedValue == null || expectedValue.isNull()) continue;

            AnnotatedMember accessor = prop.getAccessor();
            if (accessor == null) continue;

            Object actualValue = accessor.getValue(instance);
            verifyPopulated(prop.getPrimaryType(), actualValue, expectedValue, path + "." + prop.getName());
        }
    }

    private Path fileFor(PayloadType payload) {
        return jsonDirectory()
                .resolve(payload.direction().name().toLowerCase())
                .resolve(payload.type().getRawClass().getName() + ".json");
    }
}
