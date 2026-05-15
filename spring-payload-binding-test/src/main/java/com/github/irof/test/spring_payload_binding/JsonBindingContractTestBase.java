package com.github.irof.test.spring_payload_binding;

import com.github.irof.test.spring_payload_binding.jackson.JacksonContextProvider;
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
     * コンストラクタ
     * フィールドはAutowiredするのでコンストラクタでは受け取らない
     */
    public JsonBindingContractTestBase() {
    }

    /**
     * ペイロード型ごとのテストコンテキストを収集します。
     * デフォルト実装はクラスパスから Jackson バージョンを自動検出します。
     *
     * @param handlerMapping ハンドラーマッピング
     * @return テストコンテキストのコレクション
     */
    protected Collection<? extends PayloadTestContext> collectPayloadContexts(RequestMappingHandlerMapping handlerMapping) {
        return JacksonContextProvider.detectProvider().collect(handlerMapping, handlerAdapter.getMessageConverters());
    }

    /**
     * 各ペイロードに対して実行するバリエーション群を返します。
     * デフォルトは全ペイロードで {@link Variation#SAMPLE}, {@link Variation#NULL}, {@link Variation#EMPTY} です。
     *
     * @param payloadTestContext ペイロードのテストコンテキスト
     * @return バリエーションのリスト
     */
    protected List<Variation> variations(PayloadTestContext payloadTestContext) {
        return List.of(Variation.SAMPLE, Variation.NULL, Variation.EMPTY);
    }

    /**
     * JSONファイルを格納するディレクトリのパスを返します。
     * デフォルトは "src/test/resources/json-binding" です。
     *
     * @return JSONディレクトリのパス
     */
    protected Path jsonDirectory() {
        // FIXME: 相対パスなので構成や実行の仕方によって安定しない
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
    List<DynamicTest> everyEndpointPayloadTypeIsJsonBindable() throws Exception {
        boolean writeMissing = writeMissingFiles();
        List<DynamicTest> tests = new ArrayList<>();
        for (PayloadTestContext ctx : collectPayloadContexts(handlerMapping)) {
            for (Variation variation : variations(ctx)) {
                if (writeMissing) {
                    ctx.writeFixtureIfMissing(variation, jsonDirectory());
                }
                tests.add(DynamicTest.dynamicTest(
                        "[" + variation.name() + "] " + ctx.payloadName(),
                        () -> runWithContext(ctx, variation)));
            }
        }
        return tests;
    }

    private void runWithContext(PayloadTestContext ctx, Variation variation) throws Exception {
        try {
            ctx.runRoundTrip(variation, jsonDirectory());
        } catch (Throwable t) {
            String message = ctx.payloadName() + " [" + variation.name() + "] used by:\n  "
                    + String.join("\n  ", ctx.endpoints()) + "\n"
                    + (t.getMessage() != null ? t.getMessage() : t.toString());
            throw new AssertionError(message, t);
        }
    }

}
