package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.QueryNodeVisitor;
import io.github.jsoninsight.query.ast.QueryLiteralNode;
import io.github.jsoninsight.query.ast.operator.ComparisonOperator;

public record ComparisonNode(
    JsonPathNode path,
    ComparisonOperator operator,
    QueryLiteralNode rightValue
) implements QueryExpressionNode {
    @Override
    public <T> T accept(QueryNodeVisitor<T> visitor) {
        return visitor.visitComparison(this);
    }
}
