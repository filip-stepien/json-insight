package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.expression.node.JsonPathNode;

import java.util.Optional;

public record EvaluationContext(JsonNode document) {

    public JsonNode resolvePath(JsonPathNode path) {
        return resolvePathOptional(path)
            .orElseThrow(() -> new QueryExpressionEvaluatorException("Path not found: " + path.pathValue()));
    }

    public boolean pathExists(JsonPathNode path) {
        return resolvePathOptional(path).isPresent();
    }

    private Optional<JsonNode> resolvePathOptional(JsonPathNode path) {
        String[] segments = path.pathValue().substring(1).split("\\.");
        JsonNode current = document;

        for (String segment : segments) {
            if (!(current instanceof JsonNode.ObjectNode objectNode)) {
                return Optional.empty();
            }

            JsonNode next = objectNode.fields().get(segment);
            if (next == null) {
                return Optional.empty();
            }

            current = next;
        }

        return Optional.of(current);
    }
}
