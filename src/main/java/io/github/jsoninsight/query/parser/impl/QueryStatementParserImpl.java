package io.github.jsoninsight.query.parser.impl;

import io.github.jsoninsight.query.ast.statement.QueryStatement;
import io.github.jsoninsight.query.ast.statement.clause.FromClause;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.ast.statement.clause.WhereClause;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.QueryTokenType;
import io.github.jsoninsight.query.parser.QueryParser;
import io.github.jsoninsight.query.parser.QueryStatementParser;
import io.github.jsoninsight.query.parser.QueryStatementParserException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class QueryStatementParserImpl implements QueryStatementParser {

    private final QueryParser<SelectClause> selectClauseParser;
    private final QueryParser<FromClause> fromClauseParser;
    private final QueryParser<WhereClause> whereClauseParser;

    private OptionalInt findFirstTokenType(QueryTokenType type, List<QueryToken> tokens) {
        return IntStream.range(0, tokens.size())
            .filter(index -> tokens.get(index).type() == type)
            .findFirst();
    }

    private List<QueryToken> sliceTokens(int from, int to, List<QueryToken> tokens) {
        return new ArrayList<>(tokens.subList(from, to));
    }

    @Override
    public QueryStatement parse(List<QueryToken> tokens) {
        int fromKeywordPos = findFirstTokenType(QueryTokenType.FROM, tokens)
            .orElseThrow(() -> new QueryStatementParserException("Missing FROM clause", null, 0));
        OptionalInt whereKeywordPos = findFirstTokenType(QueryTokenType.WHERE, tokens);

        List<QueryToken> selectClauseTokens = sliceTokens(0, fromKeywordPos, tokens);
        SelectClause selectClause = selectClauseParser.parse(selectClauseTokens);

        int lastTokenPos = tokens.size() - 1;
        int fromClauseEndPos = whereKeywordPos.orElse(lastTokenPos);
        List<QueryToken> fromClauseTokens = sliceTokens(fromKeywordPos, fromClauseEndPos, tokens);
        FromClause fromClause = fromClauseParser.parse(fromClauseTokens);

        Optional<WhereClause> whereClause = Optional.empty();
        if (whereKeywordPos.isPresent()) {
            List<QueryToken> whereClauseTokens = sliceTokens(whereKeywordPos.getAsInt(), tokens.size(), tokens);
            whereClause = Optional.of(whereClauseParser.parse(whereClauseTokens));
        }

        return new QueryStatement(selectClause, fromClause, whereClause);
    }
}
