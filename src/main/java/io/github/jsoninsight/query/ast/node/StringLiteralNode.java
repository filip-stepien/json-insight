package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryArgVisitor;
import io.github.jsoninsight.query.ast.QueryLiteralNode;

public record StringLiteralNode(String value) implements QueryLiteralNode {
    @Override
    public <T> T accept(QueryArgVisitor<T> visitor) {
        return visitor.visitStringLiteral(this);
    }
}
