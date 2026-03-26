package io.github.jsoninsight.json;

public record Token(TokenType type, String value, int line, int column) {

}