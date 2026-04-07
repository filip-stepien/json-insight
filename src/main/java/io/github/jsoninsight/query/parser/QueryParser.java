package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.lexer.QueryToken;

import java.util.List;

public interface QueryParser {

    QueryExpressionNode parse(List<QueryToken> tokens);
}
