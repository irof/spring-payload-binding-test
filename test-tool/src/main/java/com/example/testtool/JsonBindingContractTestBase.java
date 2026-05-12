package com.example.testtool;

import com.example.testtool.EndpointPayloadTypes.PayloadType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * 各ペイロードに対して実行するバリエーション群を返す。型ごとに自由に
     * 組み替え可能 (NULL を受け付けない型はリストから外す、特定エンドポイントだけ
     * カスタムバリエーションを追加する、等)。デフォルトは全ペイロードで SAMPLE と NULL。
     */
    protected List<Variation> variations(PayloadType payload) {
        return List.of(Variation.SAMPLE, Variation.NULL);
    }

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        Mode mode = mode();
        List<DynamicTest> tests = new ArrayList<>();
        for (PayloadType payload : EndpointPayloadTypes.collect(handlerMapping, objectMapper)) {
            for (Variation variation : variations(payload)) {
                tests.add(DynamicTest.dynamicTest(
                        "[" + mode + "][" + variation.name() + "] " + payload.type().toCanonical(),
                        () -> run(payload, variation, mode)));
            }
        }
        return tests;
    }

    private void run(PayloadType payload, Variation variation, Mode mode) throws Exception {
        try {
            runChecked(payload, variation, mode);
        } catch (Throwable t) {
            String message = payload.type().toCanonical() + " [" + variation.name() + "] used by:\n  "
                    + String.join("\n  ", payload.endpoints()) + "\n"
                    + (t.getMessage() != null ? t.getMessage() : t.toString());
            throw new AssertionError(message, t);
        }
    }

    private void runChecked(PayloadType payload, Variation variation, Mode mode) throws Exception {
        JsonNode source = (mode == Mode.VERIFY) ? loadFromFile(payload, variation) : variation.build(payload.type(), objectMapper);
        String sourceJson = objectMapper.writeValueAsString(source);

        log.info("[{}][{}] {}\n{}", mode, variation.name(), payload.type().toCanonical(), source.toPrettyString());

        Object instance = objectMapper.readValue(sourceJson, payload.type());
        String serialized = objectMapper.writeValueAsString(instance);
        JsonNode normalizedSource = objectMapper.readTree(sourceJson);
        JsonNode actual = objectMapper.readTree(serialized);
        assertEquals(normalizedSource, actual, "round-trip JSON differs from source");

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

    private Path fileFor(PayloadType payload, Variation variation) {
        return jsonDirectory()
                .resolve(payload.type().getRawClass().getName())
                .resolve(variation.name() + ".json");
    }
}
