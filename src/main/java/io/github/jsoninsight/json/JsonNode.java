package io.github.jsoninsight.json;

import java.util.List;
import java.util.Map;

public sealed interface JsonNode permits
        JsonNode.ObjectNode, JsonNode.ArrayNode,
        JsonNode.StringNode, JsonNode.NumberNode,
        JsonNode.BooleanNode, JsonNode.NullNode {

    record ObjectNode(Map<String, JsonNode> fields) implements JsonNode {}

    record ArrayNode(List<JsonNode> elements) implements JsonNode {}

    record StringNode(String value) implements JsonNode {}

    record NumberNode(String raw, boolean isFloat) implements JsonNode {}

    record BooleanNode(boolean value) implements JsonNode {}

    record NullNode() implements JsonNode {}
}
