package io.github.jsoninsight.query.ast.statement.clause;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.statement.QueryStatementNode;

public record WhereClause(QueryPredicateExpression predicate) implements QueryStatementNode {}
