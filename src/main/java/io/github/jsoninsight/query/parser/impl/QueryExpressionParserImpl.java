package io.github.jsoninsight.query.parser.impl;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.expression.node.*;
import io.github.jsoninsight.query.ast.expression.operator.ComparisonOperator;
import io.github.jsoninsight.query.ast.expression.operator.JsonType;
import io.github.jsoninsight.query.ast.expression.operator.LogicalOperator;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.QueryTokenType;
import io.github.jsoninsight.query.parser.QueryExpressionParser;
import io.github.jsoninsight.query.parser.QueryExpressionParserException;

import java.math.BigDecimal;
import java.util.List;

public class QueryExpressionParserImpl implements QueryExpressionParser {

    private static final String MATCHES_FUNCTION = "matches";
    private static final String SIZE_FUNCTION = "size";

    private static class QueryExpressionParserState {
        private final List<QueryToken> tokens;
        private int pos;

        QueryExpressionParserState(List<QueryToken> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        QueryToken current() {
            return tokens.get(pos);
        }

        QueryToken consume() {
            return tokens.get(pos++);
        }

        QueryToken consume(QueryTokenType expected) {
            QueryToken token = current();
            if (token.type() != expected) {
                throw new QueryExpressionParserException(
                    "Expected " + expected + " but got " + token.type(),
                    token,
                    pos
                );
            }

            return consume();
        }

        boolean tryConsume(QueryTokenType type) {
            if (current().type() == type) {
                consume();
                return true;
            }

            return false;
        }
    }

    private boolean isComparisonOperator(QueryTokenType type) {
        return switch (type) {
            case EQ, NEQ, GT, GTE, LT, LTE -> true;
            default -> false;
        };
    }

    private boolean isFunctionNamed(QueryToken token, String name) {
        return token.type() == QueryTokenType.IDENTIFIER && token.value().equalsIgnoreCase(name);
    }

    private ComparisonOperator toComparisonOperator(QueryToken token) {
        return switch (token.type()) {
            case EQ -> ComparisonOperator.EQ;
            case NEQ -> ComparisonOperator.NEQ;
            case GT -> ComparisonOperator.GT;
            case GTE -> ComparisonOperator.GTE;
            case LT -> ComparisonOperator.LT;
            case LTE -> ComparisonOperator.LTE;
            default -> throw new QueryExpressionParserException(
                "Expected a comparison operator",
                token,
                0
            );
        };
    }

    private QueryExpression parseLiteral(QueryExpressionParserState state) {
        return switch (state.current().type()) {
            case STRING -> new StringLiteralNode(state.consume().value());

            case NUMBER -> new NumberLiteralNode(new BigDecimal(state.consume().value()));

            case BOOLEAN -> new BooleanLiteralNode(
                Boolean.parseBoolean(state.consume().value())
            );

            case NULL -> {
                state.consume();
                yield new NullLiteralNode();
            }

            default -> throw new QueryExpressionParserException(
                "Expected a literal value",
                state.current(),
                state.pos
            );
        };
    }

    private QueryExpression parseSizeFunction(QueryExpressionParserState state) {
        QueryToken name = state.consume(QueryTokenType.IDENTIFIER);
        if (!name.value().equalsIgnoreCase(SIZE_FUNCTION)) {
            throw new QueryExpressionParserException(
                "Expected size function but got " + name.value(),
                name,
                state.pos
            );
        }

        state.consume(QueryTokenType.LPAREN);
        QueryExpression value = parseValueExpression(state);
        state.consume(QueryTokenType.RPAREN);
        return new SizeNode(value);
    }

    private QueryExpression parseMatchesFunction(QueryExpressionParserState state) {
        QueryToken name = state.consume(QueryTokenType.IDENTIFIER);
        if (!name.value().equalsIgnoreCase(MATCHES_FUNCTION)) {
            throw new QueryExpressionParserException(
                "Expected matches function but got " + name.value(),
                name,
                state.pos
            );
        }

        state.consume(QueryTokenType.LPAREN);
        QueryExpression value = parseValueExpression(state);
        state.consume(QueryTokenType.COMMA);
        QueryExpression pattern = parseValueExpression(state);
        state.consume(QueryTokenType.RPAREN);
        return new MatchesNode(value, pattern);
    }

    private QueryExpression parseFunctionExpression(QueryExpressionParserState state) {
        QueryToken functionName = state.current();

        if (isFunctionNamed(functionName, MATCHES_FUNCTION)) {
            return parseMatchesFunction(state);
        }

        if (isFunctionNamed(functionName, SIZE_FUNCTION)) {
            return parseSizeFunction(state);
        }

        throw new QueryExpressionParserException(
            "Unknown function: " + functionName.value(),
            functionName,
            state.pos
        );
    }

    private QueryExpression parseValueExpression(QueryExpressionParserState state) {
        return switch (state.current().type()) {
            case JSON_PATH -> new JsonPathNode(state.consume().value());
            case STRING, NUMBER, BOOLEAN, NULL -> parseLiteral(state);
            case IDENTIFIER -> {
                if (isFunctionNamed(state.current(), SIZE_FUNCTION)) {
                    yield parseSizeFunction(state);
                }

                throw new QueryExpressionParserException(
                    "Expected a value expression",
                    state.current(),
                    state.pos
                );
            }
            default -> throw new QueryExpressionParserException(
                "Expected a value expression",
                state.current(),
                state.pos
            );
        };
    }

    private QueryExpression parsePrimaryOperand(QueryExpressionParserState state) {
        if (state.tryConsume(QueryTokenType.LPAREN)) {
            QueryExpression inner = parseOr(state);
            state.consume(QueryTokenType.RPAREN);
            return inner;
        }

        if (state.current().type() == QueryTokenType.IDENTIFIER) {
            return parseFunctionExpression(state);
        }

        return parseValueExpression(state);
    }

    private QueryExpression parsePostfixOrComparison(QueryExpressionParserState state) {
        QueryExpression left = parsePrimaryOperand(state);
        QueryToken next = state.current();

        if (left instanceof JsonPathNode path && state.tryConsume(QueryTokenType.EXISTS)) {
            return new ExistsNode(path);
        }

        if (left instanceof JsonPathNode path && state.tryConsume(QueryTokenType.IS)) {
            QueryToken typeToken = state.consume(QueryTokenType.IDENTIFIER);
            JsonType jsonType;

            try {
                jsonType = JsonType.valueOf(typeToken.value().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new QueryExpressionParserException(
                    "Unknown dataType: " + typeToken.value(),
                    typeToken,
                    state.pos
                );
            }

            return new IsNode(path, jsonType);
        }

        if (isComparisonOperator(next.type())) {
            ComparisonOperator operator = toComparisonOperator(state.consume());
            return new ComparisonNode(left, operator, parseValueExpression(state));
        }

        return left;
    }

    private QueryExpression parseNot(QueryExpressionParserState state) {
        if (state.tryConsume(QueryTokenType.NOT)) {
            return new NotNode(parseNot(state));
        }

        return parsePostfixOrComparison(state);
    }

    private QueryExpression parseAnd(QueryExpressionParserState state) {
        QueryExpression left = parseNot(state);

        while (state.tryConsume(QueryTokenType.AND)) {
            QueryExpression right = parseNot(state);
            left = new LogicalNode(left, LogicalOperator.AND, right);
        }

        return left;
    }

    private QueryExpression parseOr(QueryExpressionParserState state) {
        QueryExpression left = parseAnd(state);

        while (state.tryConsume(QueryTokenType.OR)) {
            QueryExpression right = parseAnd(state);
            left = new LogicalNode(left, LogicalOperator.OR, right);
        }

        return left;
    }

    @Override
    public QueryExpression parse(List<QueryToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Token list cannot be null or empty");
        }

        QueryExpressionParserState state = new QueryExpressionParserState(tokens);
        QueryExpression result = parseOr(state);

        state.consume(QueryTokenType.EOF);
        return result;
    }
}
