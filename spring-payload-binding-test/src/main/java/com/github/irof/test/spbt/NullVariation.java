package com.github.irof.test.spbt;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class NullVariation implements Variation {

    @Override
    public String name() {
        return "null";
    }

    @Override
    public JsonNode build(JavaType type, ObjectMapper objectMapper) {
        BeanDescription desc = objectMapper.getSerializationConfig().introspect(type);
        if (desc.findJsonValueAccessor() != null) return NullNode.instance;
        ObjectNode obj = objectMapper.createObjectNode();
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            obj.putNull(prop.getName());
        }
        return obj;
    }
}
