package com.github.irof.test.spring_payload_binding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 型ごとのカスタム値を設定するビルダーです。
 * {@link EngineVariation#customMapping} のコンシューマ引数として使用します。
 */
public final class TypeConfigurer {

    private final Map<Class<?>, Supplier<?>> values = new LinkedHashMap<>();

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
        values.put(type, () -> value);
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
        values.put(type, () -> value);
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
        values.put(type, () -> value);
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
        values.put(type, () -> value);
        return this;
    }

    /**
     * 指定した型に対して動的に値を生成する Supplier を設定します。
     *
     * @param type     対象の型
     * @param supplier サンプル値
     * @return このインスタンス
     */
    public TypeConfigurer type(Class<?> type, Supplier<?> supplier) {
        values.put(type, supplier);
        return this;
    }

    Map<Class<?>, Supplier<?>> build() {
        return Map.copyOf(values);
    }
}
