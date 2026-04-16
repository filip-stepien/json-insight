package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.lexer.QueryToken;

import java.util.List;

public interface QueryParser<T> {

    T parse(List<QueryToken> tokens);
}
