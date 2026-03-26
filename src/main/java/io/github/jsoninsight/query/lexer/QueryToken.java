package io.github.jsoninsight.query.lexer;

public record QueryToken(QueryTokenType type, String value) {

    @Override
    public String toString() {
        return "{ " + type + ": \"" + value + "\" }";
    }
}
