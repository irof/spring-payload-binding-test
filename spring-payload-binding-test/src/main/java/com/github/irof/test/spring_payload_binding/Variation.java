package com.github.irof.test.spring_payload_binding;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを生成するインタフェースです。
 * 利用側で実装すれば任意のバリエーション (例: "edge-case", "minimum") を追加できます。
 * ビルトイン定数 ({@link #SAMPLE}, {@link #NULL}, {@link #EMPTY}) は Jackson バージョンに依存せず使用できます。
 * Jackson バージョン依存の詳細な制御が必要な場合は各バージョンの Variation サブインタフェースを参照してください。
 */
public interface Variation {

    /**
     * バリエーション名を返します。
     * Filename にも使われ、同じ payload 型内でユニークである必要があります。
     *
     * @return バリエーション名
     */
    String name();

    /**
     * 全フィールドにサンプル値を埋めるビルトインバリエーションです。
     */
    Variation SAMPLE = () -> "sample";

    /**
     * 全フィールドを null にするビルトインバリエーションです。
     */
    Variation NULL = () -> "null";

    /**
     * 空/ゼロ値を埋めるビルトインバリエーションです。
     */
    Variation EMPTY = () -> "empty";
}
