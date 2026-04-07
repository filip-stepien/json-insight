package io.github.jsoninsight.query.ast;

import io.github.jsoninsight.query.ast.node.BooleanLiteralNode;
import io.github.jsoninsight.query.ast.node.NullLiteralNode;
import io.github.jsoninsight.query.ast.node.NumberLiteralNode;
import io.github.jsoninsight.query.ast.node.StringLiteralNode;

public sealed interface QueryLiteralNode extends QueryArgNode permits
    StringLiteralNode,
    NumberLiteralNode,
    BooleanLiteralNode,
    NullLiteralNode {
}
