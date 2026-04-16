package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateArgVisitor;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateLiteral;
import java.math.BigDecimal;

public record NumberLiteralNode(BigDecimal value) implements QueryPredicateLiteral {
    @Override
    public <T> T accept(QueryPredicateArgVisitor<T> visitor) {
        return visitor.visitNumberLiteral(this);
    }
}
