package com.example.testtool;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class EndpointPayloadTypes {

    public enum Direction { REQUEST, RESPONSE }

    public record PayloadType(JavaType type, Direction direction) {}

    private EndpointPayloadTypes() {}

    public static Set<PayloadType> collect(RequestMappingHandlerMapping handlerMapping, ObjectMapper objectMapper) {
        Set<PayloadType> result = new LinkedHashSet<>();
        handlerMapping.getHandlerMethods().forEach((info, handler) -> {
            if (isFrameworkHandler(handler)) return;
            for (MethodParameter p : handler.getMethodParameters()) {
                if (p.hasParameterAnnotation(RequestBody.class)) {
                    addUnwrapped(objectMapper.constructType(p.getGenericParameterType()), Direction.REQUEST, result);
                }
            }
            addUnwrapped(objectMapper.constructType(handler.getMethod().getGenericReturnType()), Direction.RESPONSE, result);
        });
        return result;
    }

    private static boolean isFrameworkHandler(HandlerMethod handler) {
        return handler.getBeanType().getPackageName().startsWith("org.springframework.");
    }

    private static void addUnwrapped(JavaType type, Direction direction, Set<PayloadType> out) {
        if (type == null) return;
        Class<?> raw = type.getRawClass();

        if (raw == Void.class || raw == void.class) return;
        if (HttpEntity.class.isAssignableFrom(raw) || Optional.class.isAssignableFrom(raw)) {
            addUnwrapped(type.containedTypeOrUnknown(0), direction, out);
            return;
        }
        if (type.isContainerType()) {
            addUnwrapped(type.getContentType(), direction, out);
            return;
        }
        if (isScalar(raw)) return;
        if (raw.getName().startsWith("java.")) return;

        out.add(new PayloadType(type, direction));
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
