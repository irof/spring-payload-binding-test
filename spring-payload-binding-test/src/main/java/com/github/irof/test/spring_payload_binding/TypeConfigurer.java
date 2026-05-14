package com.github.irof.test.spring_payload_binding;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 型ごとのカスタム値を設定するビルダーです。
 * {@link Variation#customMapping} のコンシューマ引数として使用します。
 */
public final class TypeConfigurer {

    private final Map<Class<?>, Object> values = new LinkedHashMap<>();

    TypeConfigurer() {
    }

    /**
     * 指定した型に対して String 値を設定します。
     *
     * @param type  対象の型
     * @param value サンプル値
     * @return このインスタンス
     */
    public TypeConfigurer type(Class<?> type, String value) {
        values.put(type, value);
        return this;
    }

    /**
     * 指定した型に対して int 値を設定します。
     *
     * @param type  対象の型
     * @param value サンプル値
     * @return このインスタンス
     */
    public TypeConfigurer type(Class<?> type, int value) {
        values.put(type, value);
        return this;
    }

    /**
     * 指定した型に対して long 値を設定します。
     *
     * @param type  対象の型
     * @param value サンプル値
     * @return このインスタンス
     */
    public TypeConfigurer type(Class<?> type, long value) {
        values.put(type, value);
        return this;
    }

    /**
     * 指定した型に対して boolean 値を設定します。
     *
     * @param type  対象の型
     * @param value サンプル値
     * @return このインスタンス
     */
    public TypeConfigurer type(Class<?> type, boolean value) {
        values.put(type, value);
        return this;
    }

    Map<Class<?>, Object> build() {
        return Map.copyOf(values);
    }
}
