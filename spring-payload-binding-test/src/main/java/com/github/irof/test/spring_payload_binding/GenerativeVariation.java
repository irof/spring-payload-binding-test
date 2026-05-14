package com.github.irof.test.spring_payload_binding;

/**
 * 任意の JSON 文字列を直接生成するカスタムバリエーションです。
 * {@link EngineVariation} のエンジン生成を使わず、生の JSON を返します。
 */
public interface GenerativeVariation extends Variation {

    /**
     * ペイロードクラスに対応する JSON 文字列を返します。
     *
     * @param payloadClass ペイロードの raw クラス
     * @return JSON 文字列
     */
    String buildJson(Class<?> payloadClass);
}
