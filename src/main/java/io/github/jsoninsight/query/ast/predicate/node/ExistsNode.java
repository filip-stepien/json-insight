package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpressionVisitor;

public record ExistsNode(JsonPathNode path) implements QueryPredicateExpression {
    @Override
    public <T> T accept(QueryPredicateExpressionVisitor<T> visitor) {
        return visitor.visitExists(this);
    }
}
