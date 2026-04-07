package io.github.jsoninsight.query.ast;

import io.github.jsoninsight.query.ast.node.JsonPathNode;

public sealed interface QueryArgNode extends QueryNode permits JsonPathNode, QueryLiteralNode {
}
