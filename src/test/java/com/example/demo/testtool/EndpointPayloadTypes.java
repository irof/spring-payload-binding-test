package com.example.demo.testtool;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class EndpointPayloadTypes {

    private EndpointPayloadTypes() {}

    public static Set<JavaType> collect(RequestMappingHandlerMapping handlerMapping, ObjectMapper objectMapper) {
        Set<JavaType> roots = new LinkedHashSet<>();
        handlerMapping.getHandlerMethods().forEach((info, handler) -> {
            if (isFrameworkHandler(handler)) return;
            collectRequestBodies(handler, objectMapper, roots);
            collectResponseBody(handler, objectMapper, roots);
        });
        Set<JavaType> all = new LinkedHashSet<>();
        for (JavaType root : roots) {
            walk(root, objectMapper, all);
        }
        return all;
    }

    private static boolean isFrameworkHandler(HandlerMethod handler) {
        String pkg = handler.getBeanType().getPackageName();
        return pkg.startsWith("org.springframework.");
    }

    private static void collectRequestBodies(HandlerMethod handler, ObjectMapper mapper, Set<JavaType> out) {
        for (MethodParameter p : handler.getMethodParameters()) {
            if (p.hasParameterAnnotation(RequestBody.class)) {
                out.add(mapper.constructType(p.getGenericParameterType()));
            }
        }
    }

    private static void collectResponseBody(HandlerMethod handler, ObjectMapper mapper, Set<JavaType> out) {
        out.add(mapper.constructType(handler.getMethod().getGenericReturnType()));
    }

    private static void walk(JavaType type, ObjectMapper mapper, Set<JavaType> seen) {
        if (type == null) return;
        Class<?> raw = type.getRawClass();

        if (raw == Void.class || raw == void.class) return;

        if (HttpEntity.class.isAssignableFrom(raw) || Optional.class.isAssignableFrom(raw)) {
            walk(type.containedTypeOrUnknown(0), mapper, seen);
            return;
        }
        if (type.isContainerType()) {
            walk(type.getContentType(), mapper, seen);
            if (type.isMapLikeType()) walk(type.getKeyType(), mapper, seen);
            return;
        }
        if (isScalar(raw)) return;
        if (raw.getName().startsWith("java.")) return;
        if (!seen.add(type)) return;

        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            walk(prop.getPrimaryType(), mapper, seen);
        }
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
