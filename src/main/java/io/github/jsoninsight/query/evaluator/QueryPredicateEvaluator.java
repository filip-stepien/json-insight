package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;

public interface QueryPredicateEvaluator {
    boolean evaluate(JsonNode document, QueryPredicateExpression predicate);
}
