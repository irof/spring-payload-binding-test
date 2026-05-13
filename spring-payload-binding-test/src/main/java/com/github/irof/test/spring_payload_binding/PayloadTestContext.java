package com.github.irof.test.spring_payload_binding;

import java.nio.file.Path;
import java.util.List;

/**
 * ペイロード型ごとの JSON バインドテスト実行コンテキストです。
 * JSON ライブラリのバージョンに依存しない形でペイロード情報とテスト実行を抽象化します。
 */
public interface PayloadTestContext {

    /**
     * テスト表示名として使うペイロードの型名を返します。
     *
     * @return 型名
     */
    String payloadName();

    /**
     * このペイロード型を使用しているエンドポイントのリストを返します。
     * テスト失敗時のエラーメッセージに使われます。
     *
     * @return エンドポイントの説明リスト
     */
    List<String> endpoints();

    /**
     * ペイロードの raw クラスを返します。
     * {@link JsonBindingContractTestBase#variations} のオーバーライドで型による分岐に使えます。
     *
     * @return raw クラス
     */
    Class<?> rawClass();

    /**
     * 指定されたバリエーションでラウンドトリップテストを実行します。
     *
     * @param variation    テストするバリエーション
     * @param jsonDirectory fixture JSON を格納するディレクトリ
     * @param writeMissing fixture ファイルが存在しない場合に書き出すかどうか
     * @throws Exception テスト失敗時
     */
    void runRoundTrip(Variation variation, Path jsonDirectory, boolean writeMissing) throws Exception;
}
