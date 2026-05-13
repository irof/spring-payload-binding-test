package com.github.irof.test.spring_payload_binding;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JSONバインディングのコントラストテストの基底クラスです。
 * エンドポイントのペイロード型を自動的に収集し、バリエーションごとのJSONバインディングテストを生成します。
 * クラスパスから Jackson2 または Jackson3 を自動検出して動作します。
 * 明示的にバージョンを指定する場合は
 * {@code com.github.irof.test.spring_payload_binding.jackson2.JsonBindingContractTestBase} または
 * {@code com.github.irof.test.spring_payload_binding.jackson3.JsonBindingContractTestBase} を継承してください。
 */
public abstract class JsonBindingContractTestBase {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    /**
     * MessageConverterの取得元となるHandlerAdapter
     */
    @Autowired
    protected RequestMappingHandlerAdapter handlerAdapter;

    /**
     * ペイロード型ごとのテストコンテキストを収集します。
     * デフォルト実装はクラスパスから Jackson バージョンを自動検出します。
     *
     * @param handlerMapping ハンドラーマッピング
     * @return テストコンテキストのコレクション
     */
    protected Collection<? extends PayloadTestContext> collectPayloadContexts(RequestMappingHandlerMapping handlerMapping) {
        return detectProvider().collect(handlerMapping, handlerAdapter.getMessageConverters());
    }

    /**
     * 各ペイロードに対して実行するバリエーション群を返します。
     * デフォルトは全ペイロードで {@link Variation#SAMPLE}, {@link Variation#NULL}, {@link Variation#EMPTY} です。
     *
     * @param ctx ペイロードのテストコンテキスト
     * @return バリエーションのリスト
     */
    protected List<Variation> variations(PayloadTestContext ctx) {
        return List.of(Variation.SAMPLE, Variation.NULL, Variation.EMPTY);
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
     *
     * @return ファイルを書き出す場合は true
     */
    protected boolean writeMissingFiles() {
        return Boolean.getBoolean("json.binding.write");
    }

    @TestFactory
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() {
        List<DynamicTest> tests = new ArrayList<>();
        for (PayloadTestContext ctx : collectPayloadContexts(handlerMapping)) {
            for (Variation variation : variations(ctx)) {
                tests.add(DynamicTest.dynamicTest(
                        "[" + variation.name() + "] " + ctx.payloadName(),
                        () -> runWithContext(ctx, variation)));
            }
        }
        return tests;
    }

    private void runWithContext(PayloadTestContext ctx, Variation variation) throws Exception {
        try {
            ctx.runRoundTrip(variation, jsonDirectory(), writeMissingFiles());
        } catch (Throwable t) {
            String message = ctx.payloadName() + " [" + variation.name() + "] used by:\n  "
                    + String.join("\n  ", ctx.endpoints()) + "\n"
                    + (t.getMessage() != null ? t.getMessage() : t.toString());
            throw new AssertionError(message, t);
        }
    }

    private JacksonContextProvider detectProvider() {
        try {
            Class.forName("tools.jackson.databind.ObjectMapper");
            return createJackson3Provider();
        } catch (ClassNotFoundException e) {
            return createJackson2Provider();
        }
    }

    // Jackson3 クラスを直接参照しているため、このメソッドはJackson3が利用可能な場合のみ呼び出すこと
    private JacksonContextProvider createJackson3Provider() {
        return new com.github.irof.test.spring_payload_binding.jackson3.JacksonContextProvider();
    }

    // Jackson2 クラスを直接参照しているため、このメソッドはJackson3が利用不可の場合のみ呼び出すこと
    private JacksonContextProvider createJackson2Provider() {
        return new com.github.irof.test.spring_payload_binding.jackson2.JacksonContextProvider();
    }
}
