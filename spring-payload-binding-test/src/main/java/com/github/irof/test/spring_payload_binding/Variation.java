package com.github.irof.test.spring_payload_binding;

/**
 * 1 つのペイロード型に対する JSON のバリエーションを表すインタフェースです。
 * エンジン駆動の場合は {@link EngineVariation}、完全カスタムの場合は {@link GenerativeVariation} を使用します。
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
