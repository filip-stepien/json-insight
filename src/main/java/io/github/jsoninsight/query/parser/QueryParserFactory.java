package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.parser.impl.FromClauseParserImpl;
import io.github.jsoninsight.query.parser.impl.QueryExpressionParserImpl;
import io.github.jsoninsight.query.parser.impl.QueryStatementParserImpl;
import io.github.jsoninsight.query.parser.impl.SelectClauseParserImpl;
import io.github.jsoninsight.query.parser.impl.WhereClauseParserImpl;

public class QueryParserFactory {

    public static QueryStatementParser createStatementParser() {
        QueryExpressionParser expressionParser = new QueryExpressionParserImpl();
        return new QueryStatementParserImpl(
            new SelectClauseParserImpl(),
            new FromClauseParserImpl(),
            new WhereClauseParserImpl(expressionParser)
        );
    }
}
