package io.github.jsoninsight.query.ast.statement;

import io.github.jsoninsight.query.ast.statement.clause.FromClause;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.ast.statement.clause.WhereClause;

import java.util.Optional;

public record QueryStatement(
    SelectClause select,
    FromClause from,
    Optional<WhereClause> where
) {}
