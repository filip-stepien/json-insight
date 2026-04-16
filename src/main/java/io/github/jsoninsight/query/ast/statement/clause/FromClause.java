package io.github.jsoninsight.query.ast.statement.clause;

import io.github.jsoninsight.query.ast.statement.QueryStatementNode;

public record FromClause(String collectionName) implements QueryStatementNode {}
