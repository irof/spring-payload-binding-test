package com.github.irof.test.spring_payload_binding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irof.test.spring_payload_binding.EndpointPayloadTypes.PayloadType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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

    /**
     * JSONの読み書きに使用する ObjectMapper を保持します。
     */
    private ObjectMapper objectMapper;

    /**
     * ObjectMapper を取得するために使用する RequestMappingHandlerAdapter
     */
    @Autowired
    RequestMappingHandlerAdapter handlerAdapter;

    /**
     * ハンドラーメソッドの情報を取得するために使用する RequestMappingHandlerMapping
     */
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    /**
     * 使用する ObjectMapper を取得します。
     * RequestMappingHandlerAdapter に登録されている MappingJackson2HttpMessageConverter から取得します。
     *
     * @return ObjectMapper
     */
    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = handlerAdapter.getMessageConverters().stream()
                    .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                    .map(MappingJackson2HttpMessageConverter.class::cast)
                    .map(MappingJackson2HttpMessageConverter::getObjectMapper)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("MappingJackson2HttpMessageConverter not found"));
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

    /**
     * 全てのエンドポイントのペイロード型がJSONバインド可能であることを検証するテストファクトリです。
     *
     * @return 動的テストのリスト
     */
    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        ObjectMapper mapper = getObjectMapper();
        List<DynamicTest> tests = new ArrayList<>();
        for (PayloadType payload : EndpointPayloadTypes.collect(handlerMapping, mapper)) {
            for (Variation variation : variations(payload)) {
                tests.add(DynamicTest.dynamicTest(
                        "[" + variation.name() + "] " + payload.type().toCanonical(),
                        () -> run(payload, variation)));
            }
        }
        return tests;
    }

    private void run(PayloadType payload, Variation variation) throws Exception {
        try {
            runChecked(payload, variation);
        } catch (Throwable t) {
            String message = payload.type().toCanonical() + " [" + variation.name() + "] used by:\n  "
                    + String.join("\n  ", payload.endpoints()) + "\n"
                    + (t.getMessage() != null ? t.getMessage() : t.toString());
            throw new AssertionError(message, t);
        }
    }

    private void runChecked(PayloadType payload, Variation variation) throws Exception {
        ObjectMapper mapper = getObjectMapper();
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

        Object instance = mapper.readValue(sourceJson, payload.type());
        String serialized = mapper.writeValueAsString(instance);
        JsonNode normalizedSource = mapper.readTree(sourceJson);
        JsonNode actual = mapper.readTree(serialized);
        assertEquals(normalizedSource, actual, "round-trip JSON differs from source");

        if (built && writeMissingFiles()) {
            Files.createDirectories(file.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), source);
            log.info("wrote fixture: {}", file);
        }
    }

    private Path fileFor(PayloadType payload, Variation variation) {
        return jsonDirectory()
                .resolve(payload.type().getRawClass().getName())
                .resolve(variation.name() + ".json");
    }
}
