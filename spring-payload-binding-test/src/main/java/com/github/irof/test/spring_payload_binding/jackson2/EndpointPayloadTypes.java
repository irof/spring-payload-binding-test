package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.irof.test.spring_payload_binding.PayloadTypeUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * エンドポイントで使用されているペイロード型を収集するユーティリティクラスです。
 */
public final class EndpointPayloadTypes {

    /**
     * ペイロード型と、それを使用しているエンドポイントの情報のペアです。
     *
     * @param type      ペイロードの型
     * @param endpoints この型を使用しているエンドポイントのリスト
     */
    public record PayloadType(JavaType type, List<String> endpoints) {
        public Class<?> getRawClass() {
            return type.getRawClass();
        }
    }

    private EndpointPayloadTypes() {
    }

    /**
     * RequestMappingHandlerMapping からエンドポイントのペイロード型を収集します。
     *
     * @param handlerMapping HandlerMapping
     * @param objectMapper   ObjectMapper
     * @return 収集された PayloadType のセット
     */
    public static Set<PayloadType> collect(RequestMappingHandlerMapping handlerMapping, ObjectMapper objectMapper) {
        Map<JavaType, List<String>> accum = new LinkedHashMap<>();
        handlerMapping.getHandlerMethods().forEach((info, handler) -> {
            if (PayloadTypeUtils.isFrameworkHandler(handler)) return;
            String endpoint = PayloadTypeUtils.describeEndpoint(info, handler);
            for (MethodParameter p : handler.getMethodParameters()) {
                if (p.hasParameterAnnotation(RequestBody.class)) {
                    addUnwrapped(objectMapper.constructType(p.getGenericParameterType()), endpoint, accum);
                }
            }
            addUnwrapped(objectMapper.constructType(handler.getMethod().getGenericReturnType()), endpoint, accum);
        });
        Set<PayloadType> result = new LinkedHashSet<>();
        accum.forEach((type, eps) -> result.add(new PayloadType(type, List.copyOf(eps))));
        return result;
    }

    private static void addUnwrapped(JavaType type, String endpoint, Map<JavaType, List<String>> accum) {
        if (type == null) return;
        Class<?> raw = type.getRawClass();

        if (raw == Void.class || raw == void.class) return;
        if (HttpEntity.class.isAssignableFrom(raw) || Optional.class.isAssignableFrom(raw)) {
            addUnwrapped(type.containedTypeOrUnknown(0), endpoint, accum);
            return;
        }
        if (type.isContainerType()) {
            addUnwrapped(type.getContentType(), endpoint, accum);
            return;
        }
        if (PayloadTypeUtils.isScalar(raw)) return;
        if (PayloadTypeUtils.isFrameworkType(raw)) return;

        accum.computeIfAbsent(type, k -> new ArrayList<>()).add(endpoint);
    }

}
