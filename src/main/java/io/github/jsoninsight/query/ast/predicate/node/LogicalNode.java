package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpressionVisitor;
import io.github.jsoninsight.query.ast.predicate.operator.LogicalOperator;

public record LogicalNode(
    QueryPredicateExpression leftExpression,
    LogicalOperator operator,
    QueryPredicateExpression rightExpression
) implements QueryPredicateExpression {
    @Override
    public <T> T accept(QueryPredicateExpressionVisitor<T> visitor) {
        return visitor.visitLogical(this);
    }
}
