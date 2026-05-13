package com.github.irof.test.spring_payload_binding;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JSONバインディングのコントラストテストの基底クラスです。
 * エンドポイントのペイロード型を自動的に収集し、バリエーションごとのJSONバインディングテストを生成します。
 * JSON ライブラリに依存しない骨格を提供します。Jackson2 を使う場合は
 * {@code com.github.irof.test.spring_payload_binding.jackson2.JsonBindingContractTestBase} を継承してください。
 */
public abstract class JsonBindingContractTestBase {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    /**
     * ペイロード型ごとのテストコンテキストを収集します。
     * JSON ライブラリのバージョンに応じた実装を提供してください。
     *
     * @param handlerMapping ハンドラーマッピング
     * @return テストコンテキストのコレクション
     */
    protected abstract Collection<? extends PayloadTestContext> collectPayloadContexts(RequestMappingHandlerMapping handlerMapping);

    /**
     * 各ペイロードに対して実行するバリエーション群を返します。
     * 型ごとに自由に組み替え可能です (NULL を受け付けない型はリストから外す、特定エンドポイントだけ
     * カスタムバリエーションを追加する、等)。
     * デフォルトは全ペイロードで SAMPLE, NULL, EMPTY です。
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
     * Subclass で常時 true を返せば CI で全 fixture を自動 pin するような運用も可能です。
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
}
