package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryValue;

public record BooleanLiteralNode(Boolean value) implements QueryExpression {
    @Override
    public QueryValue evaluate(EvaluationContext context) {
        return QueryValue.bool(value);
    }
}
