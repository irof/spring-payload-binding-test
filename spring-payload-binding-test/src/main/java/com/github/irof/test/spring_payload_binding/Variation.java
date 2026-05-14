package com.github.irof.test.spring_payload_binding;

import java.util.function.Consumer;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを生成するインタフェースです。
 * 利用側で実装すれば任意のバリエーション (例: "edge-case", "minimum") を追加できます。
 * ビルトイン定数 ({@link #SAMPLE}, {@link #NULL}, {@link #EMPTY}) は Jackson バージョンに依存せず使用できます。
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
     * 型別カスタム値を持つバリエーションを生成します。
     * このバリエーションは呼び出し元の {@link #name()} を引き継ぎます。
     *
     * @param configurer 型ごとのカスタム値を設定するコンシューマ
     * @return カスタム値を持つバリエーション
     */
    default Variation customMapping(Consumer<TypeConfigurer> configurer) {
        TypeConfigurer mapping = new TypeConfigurer();
        configurer.accept(mapping);
        return new CustomMappingVariation(this, mapping.build());
    }

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
