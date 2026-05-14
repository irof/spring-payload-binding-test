package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.github.irof.test.spring_payload_binding.CustomMappingVariation;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import com.github.irof.test.spring_payload_binding.jackson2.EndpointPayloadTypes.PayloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Jackson2 を使用した {@link PayloadTestContext} の実装です。
 */
class Jackson2PayloadTestContext implements PayloadTestContext {

    private static final Logger log = LoggerFactory.getLogger(Jackson2PayloadTestContext.class);

    private final PayloadType payloadType;
    private final ObjectMapper mapper;

    Jackson2PayloadTestContext(PayloadType payloadType, ObjectMapper mapper) {
        this.payloadType = payloadType;
        this.mapper = mapper;
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
        Jackson2Variation j2variation = resolveVariation(variation);
        Path file = jsonDirectory
                .resolve(payloadType.getRawClass().getName())
                .resolve(variation.name() + ".json");
        JsonNode source;
        String origin;
        boolean built = !Files.exists(file);
        if (built) {
            source = j2variation.build(payloadType.type(), mapper);
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

    private static Jackson2Variation resolveVariation(Variation variation) {
        if (variation instanceof Jackson2Variation j2v) {
            return j2v;
        }
        if (variation instanceof CustomMappingVariation cmv) {
            return new CustomMappingJackson2Variation(resolveVariation(cmv.base()), toJsonNodeMap(cmv.customValues()));
        }
        return switch (variation.name()) {
            case "sample" -> Jackson2Variation.SAMPLE;
            case "null" -> Jackson2Variation.NULL;
            case "empty" -> Jackson2Variation.EMPTY;
            default -> throw new IllegalArgumentException(
                    "Unknown variation: " + variation.name() + ". Use Jackson2Variation or Variation.SAMPLE/NULL/EMPTY.");
        };
    }

    private static Map<Class<?>, JsonNode> toJsonNodeMap(Map<Class<?>, Object> values) {
        Map<Class<?>, JsonNode> result = new LinkedHashMap<>();
        for (var entry : values.entrySet()) {
            result.put(entry.getKey(), switch (entry.getValue()) {
                case String s -> TextNode.valueOf(s);
                case Integer i -> IntNode.valueOf(i);
                case Long l -> LongNode.valueOf(l);
                case Boolean b -> BooleanNode.valueOf(b);
                default -> throw new IllegalArgumentException("Unsupported value type: " + entry.getValue().getClass());
            });
        }
        return result;
    }
}
