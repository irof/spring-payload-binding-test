package com.github.irof.test.spring_payload_binding;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを生成するインタフェースです。
 * 利用側で実装すれば任意のバリエーション (例: "edge-case", "minimum") を追加できます。
 * JSON ライブラリのバージョンに依存したビルトイン定数は各バージョンの Variation サブインタフェースを参照してください。
 */
public interface Variation {

    /**
     * バリエーション名を返します。
     * Filename にも使われ、同じ payload 型内でユニークである必要があります。
     *
     * @return バリエーション名
     */
    String name();
}
