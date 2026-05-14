package com.github.irof.test.spring_payload_binding.jackson;

import java.util.List;
import java.util.Optional;

/**
 * Jackson のシリアライゼーション・イントロスペクション機能を抽象化します。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 */
public interface TypeIntrospectionAdapter<T> {

    /**
     * {@code @JsonValue} アノテーションが付いたアクセサーの型を返します。存在しない場合は空を返します。
     */
    Optional<T> findJsonValueType(T type);

    /**
     * シリアライズ対象のプロパティ一覧を返します。
     */
    List<PropertyDef<T>> findProperties(T type);

    record PropertyDef<P>(String name, P type) {
    }
}
