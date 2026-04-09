package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryArgVisitor;
import io.github.jsoninsight.query.ast.QueryLiteralNode;
import java.math.BigDecimal;

public record NumberLiteralNode(BigDecimal value) implements QueryLiteralNode {
    @Override
    public <T> T accept(QueryArgVisitor<T> visitor) {
        return visitor.visitNumberLiteral(this);
    }
}
