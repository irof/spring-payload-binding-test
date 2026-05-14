package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.github.irof.test.spring_payload_binding.JacksonAdapter;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Jackson2Adapter implements JacksonAdapter<JavaType, JsonNode> {

    private final ObjectMapper mapper;

    Jackson2Adapter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isReferenceType(JavaType type) { return type.isReferenceType(); }

    @Override
    public JavaType referencedType(JavaType type) { return type.getReferencedType(); }

    @Override
    public boolean isCollectionLike(JavaType type) { return type.isArrayType() || type.isCollectionLikeType(); }

    @Override
    public JavaType contentType(JavaType type) { return type.getContentType(); }

    @Override
    public boolean isMapLike(JavaType type) { return type.isMapLikeType(); }

    @Override
    public JavaType keyType(JavaType type) { return type.getKeyType(); }

    @Override
    public Class<?> rawClass(JavaType type) { return type.getRawClass(); }

    @Override
    public boolean isContainerType(JavaType type) { return type.isContainerType(); }

    @Override
    public JavaType constructType(Type type) { return mapper.constructType(type); }

    @Override
    public JavaType containedTypeOrUnknown(JavaType type, int index) { return type.containedTypeOrUnknown(index); }

    @Override
    public String toCanonical(JavaType type) { return type.toCanonical(); }

    @Override
    public Optional<JavaType> findJsonValueType(JavaType type) {
        var desc = mapper.getSerializationConfig().introspect(type);
        var accessor = desc.findJsonValueAccessor();
        return accessor != null ? Optional.of(mapper.constructType(accessor.getType())) : Optional.empty();
    }

    @Override
    public List<PropertyDef<JavaType>> findProperties(JavaType type) {
        var desc = mapper.getSerializationConfig().introspect(type);
        return desc.findProperties().stream()
                .map(p -> new PropertyDef<>(p.getName(), p.getPrimaryType()))
                .toList();
    }

    @Override
    public JsonNode nullNode() { return NullNode.instance; }

    @Override
    public JsonNode stringNode(String value) { return TextNode.valueOf(value); }

    @Override
    public JsonNode intNode(int value) { return IntNode.valueOf(value); }

    @Override
    public JsonNode longNode(long value) { return LongNode.valueOf(value); }

    @Override
    public JsonNode boolNode(boolean value) { return BooleanNode.valueOf(value); }

    @Override
    public JsonNode floatNode(float value) { return FloatNode.valueOf(value); }

    @Override
    public JsonNode doubleNode(double value) { return DoubleNode.valueOf(value); }

    @Override
    public JsonNode objectNode(Map<String, JsonNode> fields) {
        ObjectNode obj = mapper.createObjectNode();
        fields.forEach(obj::set);
        return obj;
    }

    @Override
    public JsonNode arrayNode(List<JsonNode> elements) {
        var arr = mapper.createArrayNode();
        elements.forEach(arr::add);
        return arr;
    }

    @Override
    public String nodeToText(JsonNode node) { return node.asText(); }

    @Override
    public JsonNode primitiveToNode(Object value) {
        return switch (value) {
            case String s -> TextNode.valueOf(s);
            case Integer i -> IntNode.valueOf(i);
            case Long l -> LongNode.valueOf(l);
            case Boolean b -> BooleanNode.valueOf(b);
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        };
    }

    @Override
    public String writeValueAsString(Object value) throws Exception {
        return mapper.writeValueAsString(value);
    }

    @Override
    public JsonNode readTree(String json) throws Exception {
        return mapper.readTree(json);
    }

    @Override
    public JsonNode readTree(File file) throws Exception {
        return mapper.readTree(file);
    }

    @Override
    public Object readValue(String json, JavaType type) throws Exception {
        return mapper.readValue(json, type);
    }

    @Override
    @SuppressWarnings("removal")
    public void writePrettyValue(File file, JsonNode value) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, value);
    }

    @Override
    public String toPrettyString(JsonNode node) { return node.toPrettyString(); }
}
