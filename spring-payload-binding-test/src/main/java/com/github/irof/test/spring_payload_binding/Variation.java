package com.github.irof.test.spring_payload_binding;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを表すインタフェースです。
 */
public interface Variation {

    /**
     * バリエーション名を返します。
     * ファイル名にも使われ、同じ payload 型内でユニークである必要があります。
     *
     * @return バリエーション名
     */
    String name();
}
