package com.github.irof.test.spring_payload_binding.jackson;

/**
 * Jackson バージョン固有の API を抽象化するアダプターインタフェースです。
 *
 * @param <T> JavaType（JacksonのJava型表現）
 * @param <N> JsonNode（Jacksonのノード型）
 */
public interface JacksonAdapter<T, N>
        extends TypeQueryAdapter<T>,
                TypeIntrospectionAdapter<T>,
                NodeFactoryAdapter<T, N>,
                JsonIOAdapter<T, N> {
}
