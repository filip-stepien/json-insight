package io.github.jsoninsight.query.ast.predicate;

import io.github.jsoninsight.query.ast.predicate.node.ComparisonNode;
import io.github.jsoninsight.query.ast.predicate.node.ExistsNode;
import io.github.jsoninsight.query.ast.predicate.node.FunctionCallNode;
import io.github.jsoninsight.query.ast.predicate.node.IsNode;
import io.github.jsoninsight.query.ast.predicate.node.LogicalNode;
import io.github.jsoninsight.query.ast.predicate.node.NotNode;

public interface QueryPredicateExpressionVisitor<T> {
    T visitComparison(ComparisonNode node);
    T visitLogical(LogicalNode node);
    T visitNot(NotNode node);
    T visitExists(ExistsNode node);
    T visitIs(IsNode node);
    T visitFunctionCall(FunctionCallNode node);
}
