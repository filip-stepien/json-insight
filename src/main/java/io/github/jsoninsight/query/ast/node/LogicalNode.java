package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.QueryExpressionVisitor;
import io.github.jsoninsight.query.ast.operator.LogicalOperator;

public record LogicalNode(
    QueryExpressionNode leftExpression,
    LogicalOperator operator,
    QueryExpressionNode rightExpression
) implements QueryExpressionNode {
    @Override
    public <T> T accept(QueryExpressionVisitor<T> visitor) {
        return visitor.visitLogical(this);
    }
}
