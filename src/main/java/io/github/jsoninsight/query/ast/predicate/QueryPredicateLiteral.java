package io.github.jsoninsight.query.ast.predicate;

import io.github.jsoninsight.query.ast.predicate.node.BooleanLiteralNode;
import io.github.jsoninsight.query.ast.predicate.node.NullLiteralNode;
import io.github.jsoninsight.query.ast.predicate.node.NumberLiteralNode;
import io.github.jsoninsight.query.ast.predicate.node.StringLiteralNode;

public sealed interface QueryPredicateLiteral extends QueryPredicateArg permits
    StringLiteralNode,
    NumberLiteralNode,
    BooleanLiteralNode,
    NullLiteralNode {
}
