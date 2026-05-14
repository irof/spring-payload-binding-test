package com.github.irof.test.spring_payload_binding.jackson;

import com.github.irof.test.spring_payload_binding.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link JacksonAdapter} を使用した {@link PayloadTestContext} の実装です。
 * Jackson バージョンに依存しない共通実装です。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 * @param <N> JsonNode（Jacksonのノード型）
 */
public final class PayloadTestContextImpl<T, N> implements PayloadTestContext {

    private static final Logger log = LoggerFactory.getLogger(PayloadTestContextImpl.class);

    private final EndpointPayloadTypes.PayloadType<T> payloadType;
    private final JacksonAdapter<T, N> adapter;
    private final VariationEngine<T, N> engine;

    public PayloadTestContextImpl(EndpointPayloadTypes.PayloadType<T> payloadType, JacksonAdapter<T, N> adapter) {
        this.payloadType = payloadType;
        this.adapter = adapter;
        this.engine = new VariationEngine<>(adapter);
    }

    @Override
    public String payloadName() {
        return adapter.toCanonical(payloadType.type());
    }

    @Override
    public List<String> endpoints() {
        return payloadType.endpoints();
    }

    @Override
    public Class<?> rawClass() {
        return payloadType.rawClass();
    }

    @Override
    public void runRoundTrip(Variation variation, Path jsonDirectory, boolean writeMissing) throws Exception {
        Path file = jsonDirectory
                .resolve(payloadType.rawClass().getName())
                .resolve(variation.name() + ".json");
        N source;
        String origin;
        boolean built = !Files.exists(file);
        if (built) {
            source = buildJson(variation);
            origin = "built";
        } else {
            source = adapter.readTree(file.toFile());
            origin = "file " + file;
        }
        String sourceJson = adapter.writeValueAsString(source);

        log.info("[{}] {} ({})\n{}", variation.name(), payloadName(), origin, adapter.toPrettyString(source));

        if (built && writeMissing) {
            Files.createDirectories(file.getParent());
            adapter.writePrettyValue(file.toFile(), source);
            log.info("wrote fixture: {}", file);
        }

        Object instance = adapter.readValue(sourceJson, payloadType.type());
        String serialized = adapter.writeValueAsString(instance);
        N normalizedSource = adapter.readTree(sourceJson);
        N actual = adapter.readTree(serialized);
        assertEquals(normalizedSource, actual, "round-trip JSON differs from source");
    }

    private N buildJson(Variation variation) throws Exception {
        if (variation instanceof GenerativeVariation gv) {
            return adapter.readTree(gv.buildJson(payloadType.rawClass()));
        }
        Map<Class<?>, N> customValues = resolveCustomValues(variation);
        return switch (variation.name()) {
            case "sample" -> engine.buildSample(payloadType.type(), customValues);
            case "null"   -> engine.buildNull(payloadType.type(), customValues);
            case "empty"  -> engine.buildEmpty(payloadType.type(), customValues);
            default -> throw new IllegalArgumentException(
                    "Unknown variation: " + variation.name()
                    + ". Use EngineVariation.SAMPLE/NULL/EMPTY or implement GenerativeVariation.");
        };
    }

    private Map<Class<?>, N> resolveCustomValues(Variation variation) {
        if (!(variation instanceof CustomMappingVariation cmv)) return Map.of();
        Map<Class<?>, N> result = new LinkedHashMap<>();
        cmv.customValues().forEach((k, v) -> result.put(k, adapter.primitiveToNode(v)));
        return result;
    }

}
