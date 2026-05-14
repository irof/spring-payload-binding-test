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
     * fixture ファイルが存在しない場合にエンジンで JSON を生成してファイルに書き出します。
     * テスト実行前の準備フェーズで呼び出されます。
     *
     * @param variation     書き出すバリエーション
     * @param jsonDirectory fixture JSON を格納するディレクトリ
     * @throws Exception 書き出し失敗時
     */
    void writeFixtureIfMissing(Variation variation, Path jsonDirectory) throws Exception;

    /**
     * 指定されたバリエーションでラウンドトリップテストを実行します。
     *
     * @param variation     テストするバリエーション
     * @param jsonDirectory fixture JSON を格納するディレクトリ
     * @throws Exception テスト失敗時
     */
    void runRoundTrip(Variation variation, Path jsonDirectory) throws Exception;
}
