package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryArgNode;
import io.github.jsoninsight.query.ast.QueryNodeVisitor;

public record JsonPathNode(String pathValue) implements QueryArgNode {
    @Override
    public <T> T accept(QueryNodeVisitor<T> visitor) {
        return visitor.visitJsonPath(this);
    }
}
