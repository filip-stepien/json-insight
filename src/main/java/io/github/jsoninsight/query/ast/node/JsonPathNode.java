package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryArgNode;
import io.github.jsoninsight.query.ast.QueryArgVisitor;

public record JsonPathNode(String pathValue) implements QueryArgNode {
    @Override
    public <T> T accept(QueryArgVisitor<T> visitor) {
        return visitor.visitJsonPath(this);
    }
}
