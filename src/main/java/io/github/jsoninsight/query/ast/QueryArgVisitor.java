package io.github.jsoninsight.query.ast;

import io.github.jsoninsight.query.ast.node.BooleanLiteralNode;
import io.github.jsoninsight.query.ast.node.JsonPathNode;
import io.github.jsoninsight.query.ast.node.NullLiteralNode;
import io.github.jsoninsight.query.ast.node.NumberLiteralNode;
import io.github.jsoninsight.query.ast.node.StringLiteralNode;

public interface QueryArgVisitor<T> {
    T visitJsonPath(JsonPathNode node);
    T visitStringLiteral(StringLiteralNode node);
    T visitNumberLiteral(NumberLiteralNode node);
    T visitBooleanLiteral(BooleanLiteralNode node);
    T visitNullLiteral(NullLiteralNode node);
}
