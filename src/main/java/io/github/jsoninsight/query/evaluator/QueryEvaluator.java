package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;

public interface QueryEvaluator {

    boolean evaluate(JsonNode document);
}
