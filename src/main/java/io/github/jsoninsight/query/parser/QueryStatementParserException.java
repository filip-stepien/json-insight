package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.lexer.QueryToken;

public class QueryStatementParserException extends QueryParserException {

    public QueryStatementParserException(String message, QueryToken token, int position) {
        super(message, token, position);
    }
}
