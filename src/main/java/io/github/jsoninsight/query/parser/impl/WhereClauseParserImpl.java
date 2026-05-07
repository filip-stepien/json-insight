package io.github.jsoninsight.query.parser.impl;

import io.github.jsoninsight.query.ast.statement.clause.WhereClause;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.parser.QueryParser;
import io.github.jsoninsight.query.parser.QueryExpressionParser;
import io.github.jsoninsight.query.ast.expression.QueryExpression;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class WhereClauseParserImpl implements QueryParser<WhereClause> {

    private final QueryExpressionParser expressionParser;

    @Override
    public WhereClause parse(List<QueryToken> tokens) {
        List<QueryToken> tokensWithoutWhereKeyword = tokens.subList(1, tokens.size());
        QueryExpression expression = expressionParser.parse(tokensWithoutWhereKeyword);
        return new WhereClause(expression);
    }
}
