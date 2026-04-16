package io.github.jsoninsight.query.ast.predicate;

import io.github.jsoninsight.query.ast.predicate.node.ComparisonNode;
import io.github.jsoninsight.query.ast.predicate.node.ExistsNode;
import io.github.jsoninsight.query.ast.predicate.node.FunctionCallNode;
import io.github.jsoninsight.query.ast.predicate.node.IsNode;
import io.github.jsoninsight.query.ast.predicate.node.LogicalNode;
import io.github.jsoninsight.query.ast.predicate.node.NotNode;

public sealed interface QueryPredicateExpression extends QueryPredicateNode permits
    ComparisonNode,
    LogicalNode,
    NotNode,
    ExistsNode,
    IsNode,
    FunctionCallNode {

    <T> T accept(QueryPredicateExpressionVisitor<T> visitor);
}
