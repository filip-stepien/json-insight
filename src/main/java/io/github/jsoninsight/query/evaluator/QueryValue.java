package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.evaluator.values.ArrayValue;
import io.github.jsoninsight.query.evaluator.values.BooleanValue;
import io.github.jsoninsight.query.evaluator.values.NullValue;
import io.github.jsoninsight.query.evaluator.values.NumberValue;
import io.github.jsoninsight.query.evaluator.values.ObjectValue;
import io.github.jsoninsight.query.evaluator.values.StringValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public sealed interface QueryValue permits
    ArrayValue,
    BooleanValue,
    NullValue,
    NumberValue,
    ObjectValue,
    StringValue {

    static QueryValue fromJsonNode(JsonNode node) {
        return switch (node) {
            case JsonNode.StringNode(String value) -> new StringValue(value);
            case JsonNode.NumberNode(String raw, boolean ignored) -> new NumberValue(new BigDecimal(raw));
            case JsonNode.BooleanNode(boolean value) -> new BooleanValue(value);
            case JsonNode.NullNode ignored -> new NullValue();
            case JsonNode.ArrayNode(List<JsonNode> elements) -> new ArrayValue(
                elements.stream().map(QueryValue::fromJsonNode).toList()
            );
            case JsonNode.ObjectNode(Map<String, JsonNode> fields) -> new ObjectValue(
                fields.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> fromJsonNode(entry.getValue())
                    ))
            );
        };
    }

    static QueryValue string(String value) {
        return new StringValue(value);
    }

    static QueryValue number(BigDecimal value) {
        return new NumberValue(value);
    }

    static QueryValue bool(boolean value) {
        return new BooleanValue(value);
    }

    static QueryValue nullValue() {
        return new NullValue();
    }

    default String asString() {
        throw new QueryExpressionEvaluatorException("Expected string value");
    }

    default BigDecimal asNumber() {
        throw new QueryExpressionEvaluatorException("Expected number value");
    }

    default boolean asBoolean() {
        throw new QueryExpressionEvaluatorException("Expected boolean value");
    }

    default int size() {
        throw new QueryExpressionEvaluatorException("Expected array, string or object value");
    }
}
