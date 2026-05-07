package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.lexer.QueryToken;

public class QueryExpressionParserException extends QueryParserException {

    public QueryExpressionParserException(String message, QueryToken token, int position) {
        super(message, token, position);
    }
}
