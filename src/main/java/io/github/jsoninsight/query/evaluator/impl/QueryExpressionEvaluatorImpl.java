package io.github.jsoninsight.query.evaluator.impl;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryExpressionEvaluator;
import io.github.jsoninsight.query.evaluator.QueryValue;

public class QueryExpressionEvaluatorImpl implements QueryExpressionEvaluator {

    @Override
    public QueryValue evaluate(JsonNode document, QueryExpression expression) {
        return expression.evaluate(new EvaluationContext(document));
    }
}
