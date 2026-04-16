package io.github.jsoninsight.query.parser.impl;

import io.github.jsoninsight.query.ast.predicate.node.JsonPathNode;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.QueryTokenType;
import io.github.jsoninsight.query.parser.QueryParser;
import io.github.jsoninsight.query.parser.QueryStatementParserException;

import java.util.ArrayList;
import java.util.List;

public class SelectClauseParserImpl implements QueryParser<SelectClause> {

    @Override
    public SelectClause parse(List<QueryToken> tokens) {
        QueryToken selectKeywordToken = tokens.getFirst();

        if (selectKeywordToken.type() != QueryTokenType.SELECT) {
            throw new QueryStatementParserException(
                "Expected SELECT but got " + selectKeywordToken.type(),
                selectKeywordToken,
                0
            );
        }

        if (tokens.size() < 2) {
            throw new QueryStatementParserException(
                "Expected field or * after SELECT",
                selectKeywordToken,
                0
            );
        }

        if (tokens.get(1).type() == QueryTokenType.ASTERISK) {
            return new SelectClause.Wildcard();
        }

        List<JsonPathNode> paths = new ArrayList<>();
        int pos = 1;

        while (pos < tokens.size()) {
            QueryToken currentToken = tokens.get(pos);

            if (currentToken.type() != QueryTokenType.JSON_PATH) {
                break;
            }

            paths.add(new JsonPathNode(currentToken.value()));
            pos++;

            if (pos >= tokens.size()) {
                break;
            }

            QueryToken nextToken = tokens.get(pos);
            if (nextToken.type() != QueryTokenType.COMMA) {
                throw new QueryStatementParserException(
                    "Expected comma between fields but got " + nextToken.type(),
                    nextToken,
                    pos
                );
            }

            pos++;

            if (pos >= tokens.size() || tokens.get(pos).type() != QueryTokenType.JSON_PATH) {
                QueryToken afterComma = tokens.get(pos - 1);
                throw new QueryStatementParserException(
                    "Expected field after comma",
                    afterComma,
                    pos - 1
                );
            }
        }

        if (paths.isEmpty()) {
            QueryToken afterSelect = tokens.get(1);
            throw new QueryStatementParserException(
                "Expected field or * after SELECT but got " + afterSelect.type(),
                afterSelect,
                1
            );
        }

        return new SelectClause.Fields(paths);
    }
}
