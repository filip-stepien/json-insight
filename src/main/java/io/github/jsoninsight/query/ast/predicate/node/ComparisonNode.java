package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpressionVisitor;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateLiteral;
import io.github.jsoninsight.query.ast.predicate.operator.ComparisonOperator;

public record ComparisonNode(
    JsonPathNode path,
    ComparisonOperator operator,
    QueryPredicateLiteral rightValue
) implements QueryPredicateExpression {
    @Override
    public <T> T accept(QueryPredicateExpressionVisitor<T> visitor) {
        return visitor.visitComparison(this);
    }
}
