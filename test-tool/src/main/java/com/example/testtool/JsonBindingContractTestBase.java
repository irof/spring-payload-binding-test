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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class JsonBindingContractTestBase {

    private static final Logger log = LoggerFactory.getLogger(JsonBindingContractTestBase.class);

    public enum Mode { SAMPLE, WRITE, VERIFY }

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    protected Mode defaultMode() {
        return Mode.SAMPLE;
    }

    protected final Mode mode() {
        String override = System.getProperty("json.binding.mode");
        return override != null ? Mode.valueOf(override.toUpperCase()) : defaultMode();
    }

    protected Path jsonDirectory() {
        return Path.of("src/test/resources/json-binding");
    }

    protected List<Variation> variations() {
        return List.of(Variation.SAMPLE, Variation.NULL);
    }

    /** Subclasses can opt out specific payload/variation combinations (e.g., types that reject null). */
    protected boolean shouldRun(PayloadType payload, Variation variation) {
        return true;
    }

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        Mode mode = mode();
        List<Variation> variations = variations();
        List<DynamicTest> tests = new ArrayList<>();
        for (PayloadType payload : EndpointPayloadTypes.collect(handlerMapping, objectMapper)) {
            for (Variation variation : variations) {
                if (!shouldRun(payload, variation)) continue;
                tests.add(DynamicTest.dynamicTest(
                        "[" + mode + "][" + payload.direction() + "][" + variation.name() + "] " + payload.type().toCanonical(),
                        () -> run(payload, variation, mode)));
            }
        }
        return tests;
    }

    private void run(PayloadType payload, Variation variation, Mode mode) throws Exception {
        try {
            runChecked(payload, variation, mode);
        } catch (Throwable t) {
            String message = payload.type().toCanonical() + " [" + payload.direction() + "][" + variation.name() + "] used by:\n  "
                    + String.join("\n  ", payload.endpoints()) + "\n"
                    + (t.getMessage() != null ? t.getMessage() : t.toString());
            throw new AssertionError(message, t);
        }
    }

    private void runChecked(PayloadType payload, Variation variation, Mode mode) throws Exception {
        JsonNode source = (mode == Mode.VERIFY) ? loadFromFile(payload, variation) : variation.build(payload.type(), objectMapper);
        String sourceJson = objectMapper.writeValueAsString(source);

        log.info("[{}][{}][{}] {}\n{}", mode, payload.direction(), variation.name(), payload.type().toCanonical(), source.toPrettyString());

        if (payload.direction() == Direction.REQUEST) {
            Object instance = objectMapper.readValue(sourceJson, payload.type());
            JsonNode normalizedSource = objectMapper.readTree(sourceJson);
            verifyPopulated(payload.type(), instance, normalizedSource, payload.type().getRawClass().getSimpleName());
        } else {
            Object instance = objectMapper.readValue(sourceJson, payload.type());
            String serialized = objectMapper.writeValueAsString(instance);
            JsonNode normalizedSource = objectMapper.readTree(sourceJson);
            JsonNode actual = objectMapper.readTree(serialized);
            assertEquals(normalizedSource, actual, "serialized JSON differs from source");
        }

        if (mode == Mode.WRITE) {
            writeToFile(payload, variation, source);
        }
    }

    private JsonNode loadFromFile(PayloadType payload, Variation variation) throws Exception {
        Path file = fileFor(payload, variation);
        if (!Files.exists(file)) {
            throw new AssertionError("missing JSON fixture: " + file.toAbsolutePath());
        }
        return objectMapper.readTree(file.toFile());
    }

    private void writeToFile(PayloadType payload, Variation variation, JsonNode source) throws Exception {
        Path file = fileFor(payload, variation);
        Files.createDirectories(file.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), source);
    }

    /**
     * For each value the source JSON populated, verify the deserialized instance
     * also has a non-null value at the same path. Skips value equality so custom
     * deserializers transforming the value don't trigger false positives.
     */
    private void verifyPopulated(JavaType type, Object instance, JsonNode expected, String path) throws Exception {
        if (expected == null || expected.isNull()) return;
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
        if (raw.isPrimitive() || raw.isEnum() || EndpointPayloadTypes.isFrameworkType(raw)) return;

        BeanDescription desc = objectMapper.getSerializationConfig().introspect(type);
        AnnotatedMember jsonValue = desc.findJsonValueAccessor();
        if (jsonValue != null) {
            if (jsonValue.getValue(instance) == null) {
                throw new AssertionError(path + " @JsonValue is null after deserialization");
            }
            return;
        }
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            AnnotatedMember accessor = prop.getAccessor();
            if (accessor == null) continue;
            Object actualValue = accessor.getValue(instance);
            JsonNode expectedValue = expected.get(prop.getName());
            verifyPopulated(prop.getPrimaryType(), actualValue, expectedValue, path + "." + prop.getName());
        }
    }

    private Path fileFor(PayloadType payload, Variation variation) {
        return jsonDirectory()
                .resolve(payload.direction().name().toLowerCase())
                .resolve(payload.type().getRawClass().getName())
                .resolve(variation.name() + ".json");
    }
}
