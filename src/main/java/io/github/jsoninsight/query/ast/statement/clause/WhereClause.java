package io.github.jsoninsight.query.ast.statement.clause;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.statement.QueryStatementNode;

public record WhereClause(QueryExpression expression) implements QueryStatementNode {}
