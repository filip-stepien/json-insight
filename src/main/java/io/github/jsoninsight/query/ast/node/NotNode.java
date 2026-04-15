package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.QueryExpressionVisitor;

public record NotNode(QueryExpressionNode operand) implements QueryExpressionNode {
    @Override
    public <T> T accept(QueryExpressionVisitor<T> visitor) {
        return visitor.visitNot(this);
    }
}
