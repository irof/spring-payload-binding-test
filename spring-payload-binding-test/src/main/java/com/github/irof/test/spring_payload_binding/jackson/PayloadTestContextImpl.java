package com.github.irof.test.spring_payload_binding.jackson;

import com.github.irof.test.spring_payload_binding.CustomMappingVariation;
import com.github.irof.test.spring_payload_binding.EmptyVariation;
import com.github.irof.test.spring_payload_binding.EngineVariation;
import com.github.irof.test.spring_payload_binding.NullVariation;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.SampleVariation;
import com.github.irof.test.spring_payload_binding.Variation;
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

    /**
     * コンストラクタ。
     *
     * @param payloadType ペイロード型情報
     * @param adapter     Jackson バージョン固有の API を抽象化するアダプター
     */
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
    public void writeFixtureIfMissing(Variation variation, Path jsonDirectory) throws Exception {
        Path file = jsonDirectory
                .resolve(payloadType.rawClass().getName())
                .resolve(variation.name() + ".json");
        if (Files.exists(file)) return;
        N json = buildJson(variation);
        Files.createDirectories(file.getParent());
        adapter.writePrettyValue(file.toFile(), json);
        log.info("wrote fixture: {}", file);
    }

    @Override
    public void runRoundTrip(Variation variation, Path jsonDirectory) throws Exception {
        Path file = jsonDirectory
                .resolve(payloadType.rawClass().getName())
                .resolve(variation.name() + ".json");
        N source;
        String origin;
        if (Files.exists(file)) {
            source = adapter.readTree(file.toFile());
            origin = "file " + file;
        } else {
            source = buildJson(variation);
            origin = "built";
        }
        String sourceJson = adapter.writeValueAsString(source);

        log.info("[{}] {} ({})\n{}", variation.name(), payloadName(), origin, adapter.toPrettyString(source));

        Object instance = adapter.readValue(sourceJson, payloadType.type());
        String serialized = adapter.writeValueAsString(instance);
        N normalizedSource = adapter.readTree(sourceJson);
        N actual = adapter.readTree(serialized);
        assertEquals(normalizedSource, actual, "round-trip JSON differs from source");
    }

    private N buildJson(Variation variation) {
        Map<Class<?>, N> customValues = resolveCustomValues(variation);
        EngineVariation base = resolveBase(variation);
        if (base instanceof SampleVariation) return engine.buildSample(payloadType.type(), customValues);
        if (base instanceof NullVariation) return engine.buildNull(payloadType.type(), customValues);
        if (base instanceof EmptyVariation) return engine.buildEmpty(payloadType.type(), customValues);
        throw new IllegalArgumentException("Unknown EngineVariation: " + base.getClass().getName());
    }

    private EngineVariation resolveBase(Variation variation) {
        if (variation instanceof CustomMappingVariation cmv) {
            return resolveBase(cmv.base());
        }
        return (EngineVariation) variation;
    }

    private Map<Class<?>, N> resolveCustomValues(Variation variation) {
        if (!(variation instanceof CustomMappingVariation cmv)) return Map.of();
        Map<Class<?>, N> result = new LinkedHashMap<>();
        cmv.customValues().forEach((k, v) -> result.put(k, adapter.primitiveToNode(v.get())));
        return result;
    }

}
