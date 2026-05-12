package com.example.testtool;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
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
                .map(type -> DynamicTest.dynamicTest(
                        "[" + mode + "] " + type.toCanonical(),
                        () -> run(type, mode, sampleFactory)))
                .toList();
    }

    private void run(JavaType type, Mode mode, SampleJsonFactory sampleFactory) throws Exception {
        JsonNode source = (mode == Mode.VERIFY) ? loadFromFile(type) : sampleFactory.build(type);

        String sourceJson = objectMapper.writeValueAsString(source);
        Object instance = objectMapper.readValue(sourceJson, type);
        String roundtripJson = objectMapper.writeValueAsString(instance);

        // Re-parse both sides so numeric node types (IntNode vs LongNode, etc.) are normalized.
        JsonNode normalizedSource = objectMapper.readTree(sourceJson);
        JsonNode roundtripped = objectMapper.readTree(roundtripJson);
        assertEquals(normalizedSource, roundtripped, () -> "JSON round-trip mismatch for " + type.toCanonical());

        if (mode == Mode.WRITE) {
            writeToFile(type, source);
        }
    }

    private JsonNode loadFromFile(JavaType type) throws Exception {
        Path file = fileFor(type);
        if (!Files.exists(file)) {
            throw new AssertionError("missing JSON fixture: " + file.toAbsolutePath());
        }
        return objectMapper.readTree(file.toFile());
    }

    private void writeToFile(JavaType type, JsonNode source) throws Exception {
        Path file = fileFor(type);
        Files.createDirectories(file.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), source);
    }

    private Path fileFor(JavaType type) {
        return jsonDirectory().resolve(type.getRawClass().getName() + ".json");
    }
}
