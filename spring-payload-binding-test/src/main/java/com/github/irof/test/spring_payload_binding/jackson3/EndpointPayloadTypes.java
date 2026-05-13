package com.github.irof.test.spring_payload_binding.jackson3;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

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
     * 指定されたクラスがフレームワーク提供の型かどうかを判定します。
     *
     * @param raw 判定対象のクラス
     * @return フレームワーク提供の型であれば true
     */
    static boolean isFrameworkType(Class<?> raw) {
        String name = raw.getName();
        return name.startsWith("java.")
                || name.startsWith("jakarta.")
                || name.startsWith("javax.")
                || name.startsWith("org.springframework.");
    }

    /**
     * RequestMappingHandlerMapping からエンドポイントのペイロード型を収集します。
     *
     * @param handlerMapping HandlerMapping
     * @param mapper         ObjectMapper
     * @return 収集された PayloadType のセット
     */
    public static Set<PayloadType> collect(RequestMappingHandlerMapping handlerMapping, ObjectMapper mapper) {
        Map<JavaType, List<String>> accum = new LinkedHashMap<>();
        handlerMapping.getHandlerMethods().forEach((info, handler) -> {
            if (isFrameworkHandler(handler)) return;
            String endpoint = describeEndpoint(info, handler);
            for (MethodParameter p : handler.getMethodParameters()) {
                if (p.hasParameterAnnotation(RequestBody.class)) {
                    addUnwrapped(mapper.constructType(p.getGenericParameterType()), endpoint, accum);
                }
            }
            addUnwrapped(mapper.constructType(handler.getMethod().getGenericReturnType()), endpoint, accum);
        });
        Set<PayloadType> result = new LinkedHashSet<>();
        accum.forEach((type, eps) -> result.add(new PayloadType(type, List.copyOf(eps))));
        return result;
    }

    private static boolean isFrameworkHandler(HandlerMethod handler) {
        return handler.getBeanType().getPackageName().startsWith("org.springframework.");
    }

    private static String describeEndpoint(RequestMappingInfo info, HandlerMethod handler) {
        RequestMethodsRequestCondition methods = info.getMethodsCondition();
        String httpMethod = methods.getMethods().isEmpty() ? "ANY" : methods.getMethods().iterator().next().name();
        String path = info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatterns().stream().findFirst().map(Object::toString).orElse("?")
                : "?";
        String handlerLabel = handler.getMethod().getDeclaringClass().getSimpleName()
                + "#" + handler.getMethod().getName();
        return httpMethod + " " + path + " (" + handlerLabel + ")";
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
        if (isScalar(raw)) return;
        if (isFrameworkType(raw)) return;

        accum.computeIfAbsent(type, k -> new ArrayList<>()).add(endpoint);
    }

    private static boolean isScalar(Class<?> raw) {
        return raw.isPrimitive()
                || raw.isEnum()
                || raw == String.class
                || raw == Boolean.class
                || raw == Character.class
                || Number.class.isAssignableFrom(raw)
                || raw == Object.class;
    }
}
