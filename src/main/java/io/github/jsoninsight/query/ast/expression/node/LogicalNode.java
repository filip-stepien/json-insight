package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.expression.operator.LogicalOperator;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryValue;

public record LogicalNode(
    QueryExpression leftExpression,
    LogicalOperator operator,
    QueryExpression rightExpression
) implements QueryExpression {
    @Override
    public QueryValue evaluate(EvaluationContext context) {
        return QueryValue.bool(switch (operator) {
            case AND -> leftExpression.evaluate(context).asBoolean()
                && rightExpression.evaluate(context).asBoolean();
            case OR -> leftExpression.evaluate(context).asBoolean()
                || rightExpression.evaluate(context).asBoolean();
        });
    }
}
