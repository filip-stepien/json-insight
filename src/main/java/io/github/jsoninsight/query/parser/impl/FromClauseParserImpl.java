package io.github.jsoninsight.query.parser.impl;

import io.github.jsoninsight.query.ast.statement.clause.FromClause;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.QueryTokenType;
import io.github.jsoninsight.query.parser.QueryParser;
import io.github.jsoninsight.query.parser.QueryStatementParserException;

import java.util.List;

public class FromClauseParserImpl implements QueryParser<FromClause> {

    @Override
    public FromClause parse(List<QueryToken> tokens) {
        QueryToken fromKeywordToken = tokens.getFirst();

        if (fromKeywordToken.type() != QueryTokenType.FROM) {
            throw new QueryStatementParserException(
                "Expected FROM but got " + fromKeywordToken.type(),
                fromKeywordToken,
                0
            );
        }

        if (tokens.size() < 2) {
            throw new QueryStatementParserException(
                "Expected collection name after FROM",
                fromKeywordToken,
                0
            );
        }

        QueryToken collectionNameToken = tokens.get(1);

        if (collectionNameToken.type() != QueryTokenType.IDENTIFIER) {
            throw new QueryStatementParserException(
                "Expected collection name but got " + collectionNameToken.type(),
                collectionNameToken,
                1
            );
        }

        return new FromClause(collectionNameToken.value());
    }
}
