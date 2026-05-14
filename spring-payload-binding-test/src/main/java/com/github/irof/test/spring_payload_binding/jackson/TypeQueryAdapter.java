package com.github.irof.test.spring_payload_binding.jackson;

import java.lang.reflect.Type;

/**
 * Jackson の型トークンに対する構造的クエリを抽象化します。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 */
public interface TypeQueryAdapter<T> {

    boolean isReferenceType(T type);

    T referencedType(T type);

    boolean isCollectionLike(T type);

    T contentType(T type);

    boolean isMapLike(T type);

    T keyType(T type);

    Class<?> rawClass(T type);

    boolean isContainerType(T type);

    T constructType(Type type);

    /**
     * Nth 型パラメータを返します。存在しない場合は不明型を返します。Optional/HttpEntity のアンラップに使用します。
     */
    T containedTypeOrUnknown(T type, int index);

    /**
     * 型の正規文字列表現（例: {@code java.util.List<java.lang.String>}）を返します。
     */
    String toCanonical(T type);
}
