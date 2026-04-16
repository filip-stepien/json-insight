package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.lexer.QueryToken;

import java.util.List;

public interface QueryPredicateParser {

    QueryPredicateExpression parse(List<QueryToken> tokens);
}
