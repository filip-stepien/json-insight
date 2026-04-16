package io.github.jsoninsight.query.ast.statement.clause;

import io.github.jsoninsight.query.ast.predicate.node.JsonPathNode;
import io.github.jsoninsight.query.ast.statement.QueryStatementNode;

import java.util.List;

public sealed interface SelectClause extends QueryStatementNode permits SelectClause.Wildcard, SelectClause.Fields {

    record Wildcard() implements SelectClause {}

    record Fields(List<JsonPathNode> paths) implements SelectClause {}
}
