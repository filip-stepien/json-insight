package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryValue;

public record NotNode(QueryExpression operand) implements QueryExpression {
    @Override
    public QueryValue evaluate(EvaluationContext context) {
        return QueryValue.bool(!operand.evaluate(context).asBoolean());
    }
}
