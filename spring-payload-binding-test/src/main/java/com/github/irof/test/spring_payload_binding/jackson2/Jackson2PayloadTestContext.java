package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irof.test.spring_payload_binding.PayloadTestContext;
import com.github.irof.test.spring_payload_binding.Variation;
import com.github.irof.test.spring_payload_binding.jackson2.EndpointPayloadTypes.PayloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        Path file = jsonDirectory
                .resolve(payloadType.getRawClass().getName())
                .resolve(variation.name() + ".json");
        JsonNode source;
        String origin;
        boolean built = !Files.exists(file);
        if (built) {
            source = variation.build(payloadType.type(), mapper);
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
}
