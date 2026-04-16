package io.github.jsoninsight.query.ast.predicate.node;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateArg;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpressionVisitor;
import java.util.List;

public record FunctionCallNode(String functionName, List<QueryPredicateArg> args) implements QueryPredicateExpression {
    @Override
    public <T> T accept(QueryPredicateExpressionVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }
}
