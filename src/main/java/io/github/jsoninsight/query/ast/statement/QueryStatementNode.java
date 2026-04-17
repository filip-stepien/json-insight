package io.github.jsoninsight.query.ast.statement;

import io.github.jsoninsight.query.ast.statement.clause.FromClause;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.ast.statement.clause.WhereClause;

public sealed interface QueryStatementNode permits SelectClause, FromClause, WhereClause {
}
