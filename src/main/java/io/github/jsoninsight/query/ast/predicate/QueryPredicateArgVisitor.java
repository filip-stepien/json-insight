package io.github.jsoninsight.query.ast.predicate;

import io.github.jsoninsight.query.ast.predicate.node.BooleanLiteralNode;
import io.github.jsoninsight.query.ast.predicate.node.JsonPathNode;
import io.github.jsoninsight.query.ast.predicate.node.NullLiteralNode;
import io.github.jsoninsight.query.ast.predicate.node.NumberLiteralNode;
import io.github.jsoninsight.query.ast.predicate.node.StringLiteralNode;

public interface QueryPredicateArgVisitor<T> {
    T visitJsonPath(JsonPathNode node);
    T visitStringLiteral(StringLiteralNode node);
    T visitNumberLiteral(NumberLiteralNode node);
    T visitBooleanLiteral(BooleanLiteralNode node);
    T visitNullLiteral(NullLiteralNode node);
}
