package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.QueryExpressionVisitor;
import io.github.jsoninsight.query.ast.operator.JsonType;

public record IsNode(JsonPathNode path, JsonType dataType) implements QueryExpressionNode {
    @Override
    public <T> T accept(QueryExpressionVisitor<T> visitor) {
        return visitor.visitIs(this);
    }
}
