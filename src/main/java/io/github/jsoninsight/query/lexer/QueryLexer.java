package io.github.jsoninsight.query.lexer;

import java.util.List;

public interface QueryLexer {

    List<QueryToken> tokenize(String input);
}
