package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateArg;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateArgVisitor;

public record JsonPathNode(String pathValue) implements QueryPredicateArg {
    @Override
    public <T> T accept(QueryPredicateArgVisitor<T> visitor) {
        return visitor.visitJsonPath(this);
    }
}
