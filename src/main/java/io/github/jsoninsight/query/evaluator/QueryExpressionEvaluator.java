package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.expression.QueryExpression;

public interface QueryExpressionEvaluator {
    QueryValue evaluate(JsonNode document, QueryExpression expression);
}
