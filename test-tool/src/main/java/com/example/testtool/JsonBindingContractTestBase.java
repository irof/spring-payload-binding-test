package com.example.testtool;

import com.example.testtool.EndpointPayloadTypes.Direction;
import com.example.testtool.EndpointPayloadTypes.PayloadType;
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
                .map(payload -> DynamicTest.dynamicTest(
                        "[" + mode + "][" + payload.direction() + "] " + payload.type().toCanonical(),
                        () -> run(payload, mode, sampleFactory)))
                .toList();
    }

    private void run(PayloadType payload, Mode mode, SampleJsonFactory sampleFactory) throws Exception {
        JsonNode source = (mode == Mode.VERIFY) ? loadFromFile(payload) : sampleFactory.build(payload.type());
        String sourceJson = objectMapper.writeValueAsString(source);

        if (payload.direction() == Direction.REQUEST) {
            objectMapper.readValue(sourceJson, payload.type());
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

    private Path fileFor(PayloadType payload) {
        return jsonDirectory()
                .resolve(payload.direction().name().toLowerCase())
                .resolve(payload.type().getRawClass().getName() + ".json");
    }
}
