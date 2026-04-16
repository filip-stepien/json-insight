package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.parser.impl.FromClauseParserImpl;
import io.github.jsoninsight.query.parser.impl.QueryPredicateParserImpl;
import io.github.jsoninsight.query.parser.impl.QueryStatementParserImpl;
import io.github.jsoninsight.query.parser.impl.SelectClauseParserImpl;
import io.github.jsoninsight.query.parser.impl.WhereClauseParserImpl;

public class QueryParserFactory {

    public static QueryStatementParser createStatementParser() {
        QueryPredicateParser predicateParser = new QueryPredicateParserImpl();
        return new QueryStatementParserImpl(
            new SelectClauseParserImpl(),
            new FromClauseParserImpl(),
            new WhereClauseParserImpl(predicateParser)
        );
    }
}
