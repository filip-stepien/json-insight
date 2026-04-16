package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpressionVisitor;
import io.github.jsoninsight.query.ast.predicate.operator.JsonType;

public record IsNode(JsonPathNode path, JsonType dataType) implements QueryPredicateExpression {
    @Override
    public <T> T accept(QueryPredicateExpressionVisitor<T> visitor) {
        return visitor.visitIs(this);
    }
}
