package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateArgVisitor;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateLiteral;

public record StringLiteralNode(String value) implements QueryPredicateLiteral {
    @Override
    public <T> T accept(QueryPredicateArgVisitor<T> visitor) {
        return visitor.visitStringLiteral(this);
    }
}
