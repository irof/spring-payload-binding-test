package com.github.irof.test.spring_payload_binding.jackson;

import com.github.irof.test.spring_payload_binding.PayloadTypeUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * エンドポイントで使用されているペイロード型を収集するユーティリティクラスです。
 */
public final class EndpointPayloadTypes {

    /**
     * ペイロード型と、それを使用しているエンドポイントの情報のペアです。
     *
     * @param <T>       Jackson の型表現
     * @param type      ペイロードの型
     * @param rawClass  ペイロードの raw クラス
     * @param endpoints この型を使用しているエンドポイントのリスト
     */
    public record PayloadType<T>(T type, Class<?> rawClass, List<String> endpoints) {
    }

    private EndpointPayloadTypes() {
    }

    /**
     * RequestMappingHandlerMapping からエンドポイントのペイロード型を収集します。
     *
     * @param <T>            Jackson の型表現
     * @param <N>            Jackson のノード型
     * @param handlerMapping HandlerMapping
     * @param adapter        Jackson アダプター
     * @return 収集された PayloadType のセット
     */
    public static <T, N> Set<PayloadType<T>> collect(
            RequestMappingHandlerMapping handlerMapping, JacksonAdapter<T, N> adapter) {
        Map<T, List<String>> accum = new LinkedHashMap<>();
        handlerMapping.getHandlerMethods().forEach((info, handler) -> {
            if (PayloadTypeUtils.isFrameworkHandler(handler)) return;
            String endpoint = PayloadTypeUtils.describeEndpoint(info, handler);
            for (MethodParameter p : handler.getMethodParameters()) {
                if (p.hasParameterAnnotation(RequestBody.class)) {
                    addUnwrapped(adapter.constructType(p.getGenericParameterType()), endpoint, accum, adapter);
                }
            }
            addUnwrapped(adapter.constructType(handler.getMethod().getGenericReturnType()), endpoint, accum, adapter);
        });
        Set<PayloadType<T>> result = new LinkedHashSet<>();
        accum.forEach((type, eps) -> result.add(new PayloadType<>(type, adapter.rawClass(type), List.copyOf(eps))));
        return result;
    }

    private static <T, N> void addUnwrapped(
            T type, String endpoint, Map<T, List<String>> accum, JacksonAdapter<T, N> adapter) {
        if (type == null) return;
        Class<?> raw = adapter.rawClass(type);
        if (raw == Void.class || raw == void.class) return;
        if (HttpEntity.class.isAssignableFrom(raw) || Optional.class.isAssignableFrom(raw)) {
            addUnwrapped(adapter.containedTypeOrUnknown(type, 0), endpoint, accum, adapter);
            return;
        }
        if (adapter.isContainerType(type)) {
            addUnwrapped(adapter.contentType(type), endpoint, accum, adapter);
            return;
        }
        if (PayloadTypeUtils.isScalar(raw)) return;
        if (PayloadTypeUtils.isFrameworkType(raw)) return;
        accum.computeIfAbsent(type, k -> new ArrayList<>()).add(endpoint);
    }
}
