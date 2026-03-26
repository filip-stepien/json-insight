package io.github.jsoninsight.query.lexer;

public enum QueryTokenType {
    IDENTIFIER,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    EQ,
    NEQ,
    GT,
    GTE,
    LT,
    LTE,
    AND,
    OR,
    NOT,
    EXISTS,
    IS,
    LPAREN,
    RPAREN,
    COMMA,
    EOF
}
