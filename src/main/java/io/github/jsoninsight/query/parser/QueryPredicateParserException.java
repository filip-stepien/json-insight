package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.lexer.QueryToken;
import lombok.Getter;

@Getter
public class QueryPredicateParserException extends RuntimeException {
    private final QueryToken token;
    private final int position;

    public QueryPredicateParserException(String message, QueryToken token, int position) {
        super(message);
        this.token = token;
        this.position = position;
    }
}
