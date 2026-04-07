package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryNodeVisitor;
import io.github.jsoninsight.query.ast.QueryLiteralNode;

public record NullLiteralNode() implements QueryLiteralNode {
    @Override
    public <T> T accept(QueryNodeVisitor<T> visitor) {
        return visitor.visitNullLiteral(this);
    }
}
