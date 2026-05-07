package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.expression.operator.JsonType;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryExpressionEvaluatorException;
import io.github.jsoninsight.query.evaluator.QueryValue;

public record IsNode(JsonPathNode path, JsonType dataType) implements QueryExpression {
    @Override
    public QueryValue evaluate(EvaluationContext context) {
        throw new QueryExpressionEvaluatorException("IS expressions are not implemented yet");
    }
}
