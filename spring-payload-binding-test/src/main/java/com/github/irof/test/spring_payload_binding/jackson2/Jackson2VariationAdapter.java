package com.github.irof.test.spring_payload_binding.jackson2;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.github.irof.test.spring_payload_binding.VariationAdapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Jackson2VariationAdapter implements VariationAdapter<JavaType, JsonNode> {

    private final ObjectMapper mapper;

    Jackson2VariationAdapter(ObjectMapper mapper) {
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
}
