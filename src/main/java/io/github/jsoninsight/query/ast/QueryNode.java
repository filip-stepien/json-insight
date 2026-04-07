package io.github.jsoninsight.query.ast;

public sealed interface QueryNode permits QueryExpressionNode, QueryArgNode {

    <T> T accept(QueryNodeVisitor<T> visitor);
}
