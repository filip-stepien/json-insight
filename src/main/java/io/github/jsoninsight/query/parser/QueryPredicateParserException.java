package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.lexer.QueryToken;

public class QueryPredicateParserException extends QueryParserException {

    public QueryPredicateParserException(String message, QueryToken token, int position) {
        super(message, token, position);
    }
}
