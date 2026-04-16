package io.github.jsoninsight.query.ast.predicate;

import io.github.jsoninsight.query.ast.predicate.node.JsonPathNode;

public sealed interface QueryPredicateArg extends QueryPredicateNode permits JsonPathNode, QueryPredicateLiteral {

    <T> T accept(QueryPredicateArgVisitor<T> visitor);
}
