package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.irof.test.spring_payload_binding.jackson2.EndpointPayloadTypes.PayloadType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JSONバインディングのコントラストテストの基底クラスです。
 * エンドポイントのペイロード型を自動的に収集し、バリエーションごとのJSONバインディングテストを生成します。
 */
public abstract class JsonBindingContractTestBase {

    private static final Logger log = LoggerFactory.getLogger(JsonBindingContractTestBase.class);

    private volatile ObjectMapper objectMapper;

    @Autowired(required = false)
    private ObjectMapper autowiredObjectMapper;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    /**
     * 使用する ObjectMapper を取得します。
     * RequestMappingHandlerAdapter に登録されている MappingJackson2HttpMessageConverter から取得します。
     *
     * @return ObjectMapper
     */
    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            if (autowiredObjectMapper != null) {
                objectMapper = autowiredObjectMapper;
            } else {
                objectMapper = handlerAdapter.getMessageConverters().stream()
                        .filter(AbstractJackson2HttpMessageConverter.class::isInstance)
                        .map(AbstractJackson2HttpMessageConverter.class::cast)
                        .map(AbstractJackson2HttpMessageConverter::getObjectMapper)
                        .findFirst()
                        .orElseGet(() -> {
                            log.warn("Jackson HttpMessageConverter not found; falling back to plain ObjectMapper");
                            return JsonMapper.builder()
                                    .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
                                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                    .addModule(new JavaTimeModule())
                                    .findAndAddModules()
                                    .build();
                        });
            }
        }
        return objectMapper;
    }

    /**
     * JSONファイルを格納するディレクトリのパスを返します。
     * デフォルトは "src/test/resources/json-binding" です。
     *
     * @return JSONディレクトリのパス
     */
    protected Path jsonDirectory() {
        return Path.of("src/test/resources/json-binding");
    }

    /**
     * ファイルが存在せず build した場合に、その JSON を fixture ファイルへ書き出すかどうかを返します。
     * デフォルトはシステムプロパティ {@code -Djson.binding.write=true} を見ます。
     * Subclass で常時 true を返せば CI で全 fixture を自動 pin するような運用も可能です。
     *
     * @return ファイルを書き出す場合は true
     */
    protected boolean writeMissingFiles() {
        return Boolean.getBoolean("json.binding.write");
    }

    /**
     * 各ペイロードに対して実行するバリエーション群を返します。
     * 型ごとに自由に組み替え可能です (NULL を受け付けない型はリストから外す、特定エンドポイントだけ
     * カスタムバリエーションを追加する、等)。
     * デフォルトは全ペイロードで SAMPLE, NULL, EMPTY です。
     *
     * @param payload ペイロード型
     * @return バリエーションのリスト
     */
    protected List<Variation> variations(PayloadType payload) {
        return List.of(Variation.SAMPLE, Variation.NULL, Variation.EMPTY);
    }

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        ObjectMapper mapper = getObjectMapper();
        List<DynamicTest> tests = new ArrayList<>();
        for (PayloadType payload : EndpointPayloadTypes.collect(handlerMapping, mapper)) {
            for (Variation variation : variations(payload)) {
                tests.add(DynamicTest.dynamicTest(
                        "[" + variation.name() + "] " + payload.type().toCanonical(),
                        () -> run(mapper, payload, variation)));
            }
        }
        return tests;
    }

    private void run(ObjectMapper mapper, PayloadType payload, Variation variation) throws Exception {
        try {
            runChecked(mapper, payload, variation);
        } catch (Throwable t) {
            String message = payload.type().toCanonical() + " [" + variation.name() + "] used by:\n  "
                    + String.join("\n  ", payload.endpoints()) + "\n"
                    + (t.getMessage() != null ? t.getMessage() : t.toString());
            throw new AssertionError(message, t);
        }
    }

    private void runChecked(ObjectMapper mapper, PayloadType payload, Variation variation) throws Exception {
        Path file = fileFor(payload, variation);
        JsonNode source;
        String origin;
        boolean built = !Files.exists(file);
        if (built) {
            source = variation.build(payload.type(), mapper);
            origin = "built";
        } else {
            source = mapper.readTree(file.toFile());
            origin = "file " + file;
        }
        String sourceJson = mapper.writeValueAsString(source);

        log.info("[{}] {} ({})\n{}", variation.name(), payload.type().toCanonical(), origin, source.toPrettyString());

        if (built && writeMissingFiles()) {
            Files.createDirectories(file.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), source);
            log.info("wrote fixture: {}", file);
        }

        Object instance = mapper.readValue(sourceJson, payload.type());
        String serialized = mapper.writeValueAsString(instance);
        JsonNode normalizedSource = mapper.readTree(sourceJson);
        JsonNode actual = mapper.readTree(serialized);
        assertEquals(normalizedSource, actual, "round-trip JSON differs from source");
    }

    private Path fileFor(PayloadType payload, Variation variation) {
        return jsonDirectory()
                .resolve(payload.type().getRawClass().getName())
                .resolve(variation.name() + ".json");
    }
}
