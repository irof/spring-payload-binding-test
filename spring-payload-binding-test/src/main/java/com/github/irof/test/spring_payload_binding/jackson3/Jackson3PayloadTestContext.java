package com.github.irof.test.spring_payload_binding.jackson3;

import com.github.irof.test.spring_payload_binding.CustomMappingVariation;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import com.github.irof.test.spring_payload_binding.VariationEngine;
import com.github.irof.test.spring_payload_binding.jackson3.EndpointPayloadTypes.PayloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Jackson3 を使用した {@link PayloadTestContext} の実装です。
 */
class Jackson3PayloadTestContext implements PayloadTestContext {

    private static final Logger log = LoggerFactory.getLogger(Jackson3PayloadTestContext.class);

    private final PayloadType payloadType;
    private final ObjectMapper mapper;
    private final Jackson3VariationAdapter adapter;
    private final VariationEngine<JavaType, JsonNode> engine;

    Jackson3PayloadTestContext(PayloadType payloadType, ObjectMapper mapper) {
        this.payloadType = payloadType;
        this.mapper = mapper;
        this.adapter = new Jackson3VariationAdapter(mapper);
        this.engine = new VariationEngine<>(adapter);
    }

    @Override
    public String payloadName() {
        return payloadType.type().toCanonical();
    }

    @Override
    public List<String> endpoints() {
        return payloadType.endpoints();
    }

    @Override
    public Class<?> rawClass() {
        return payloadType.getRawClass();
    }

    @Override
    public void runRoundTrip(Variation variation, Path jsonDirectory, boolean writeMissing) throws Exception {
        Path file = jsonDirectory
                .resolve(payloadType.getRawClass().getName())
                .resolve(variation.name() + ".json");
        JsonNode source;
        String origin;
        boolean built = !Files.exists(file);
        if (built) {
            source = buildJson(variation);
            origin = "built";
        } else {
            source = mapper.readTree(file.toFile());
            origin = "file " + file;
        }
        String sourceJson = mapper.writeValueAsString(source);

        log.info("[{}] {} ({})\n{}", variation.name(), payloadName(), origin, source.toPrettyString());

        if (built && writeMissing) {
            Files.createDirectories(file.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), source);
            log.info("wrote fixture: {}", file);
        }

        Object instance = mapper.readValue(sourceJson, payloadType.type());
        String serialized = mapper.writeValueAsString(instance);
        JsonNode normalizedSource = mapper.readTree(sourceJson);
        JsonNode actual = mapper.readTree(serialized);
        assertEquals(normalizedSource, actual, "round-trip JSON differs from source");
    }

    private JsonNode buildJson(Variation variation) {
        Map<Class<?>, JsonNode> customValues = resolveCustomValues(variation);
        return switch (resolveBaseName(variation)) {
            case "sample" -> engine.buildSample(payloadType.type(), customValues);
            case "null"   -> engine.buildNull(payloadType.type(), customValues);
            case "empty"  -> engine.buildEmpty(payloadType.type(), customValues);
            default -> throw new IllegalArgumentException(
                    "Unknown variation: " + variation.name() + ". Use Variation.SAMPLE/NULL/EMPTY.");
        };
    }

    private Map<Class<?>, JsonNode> resolveCustomValues(Variation variation) {
        if (!(variation instanceof CustomMappingVariation cmv)) return Map.of();
        Map<Class<?>, JsonNode> result = new LinkedHashMap<>();
        cmv.customValues().forEach((k, v) -> result.put(k, adapter.primitiveToNode(v)));
        return result;
    }

    private static String resolveBaseName(Variation variation) {
        return variation instanceof CustomMappingVariation cmv ? resolveBaseName(cmv.base()) : variation.name();
    }
}
