package io.github.jsoninsight.query.ast;

import io.github.jsoninsight.query.ast.node.BooleanLiteralNode;
import io.github.jsoninsight.query.ast.node.ComparisonNode;
import io.github.jsoninsight.query.ast.node.ExistsNode;
import io.github.jsoninsight.query.ast.node.FunctionCallNode;
import io.github.jsoninsight.query.ast.node.IsNode;
import io.github.jsoninsight.query.ast.node.JsonPathNode;
import io.github.jsoninsight.query.ast.node.LogicalNode;
import io.github.jsoninsight.query.ast.node.NotNode;
import io.github.jsoninsight.query.ast.node.NullLiteralNode;
import io.github.jsoninsight.query.ast.node.NumberLiteralNode;
import io.github.jsoninsight.query.ast.node.StringLiteralNode;

public interface QueryNodeVisitor<T> {
    T visitComparison(ComparisonNode node);
    T visitLogical(LogicalNode node);
    T visitNot(NotNode node);
    T visitExists(ExistsNode node);
    T visitIs(IsNode node);
    T visitFunctionCall(FunctionCallNode node);
    T visitJsonPath(JsonPathNode node);
    T visitStringLiteral(StringLiteralNode node);
    T visitNumberLiteral(NumberLiteralNode node);
    T visitBooleanLiteral(BooleanLiteralNode node);
    T visitNullLiteral(NullLiteralNode node);
}
