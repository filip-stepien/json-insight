package io.github.jsoninsight.query.lexer;

import lombok.Getter;

@Getter
public class QueryLexerException extends RuntimeException {
    private final char character;
    private final int position;

    public QueryLexerException(String message, char character, int position) {
        super(message);
        this.character = character;
        this.position = position;
    }
}
