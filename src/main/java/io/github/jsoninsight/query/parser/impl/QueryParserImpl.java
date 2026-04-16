package io.github.jsoninsight.query.parser.impl;

import io.github.jsoninsight.query.ast.predicate.QueryPredicateArg;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateLiteral;
import java.math.BigDecimal;
import io.github.jsoninsight.query.ast.predicate.node.*;
import io.github.jsoninsight.query.ast.predicate.operator.ComparisonOperator;
import io.github.jsoninsight.query.ast.predicate.operator.JsonType;
import io.github.jsoninsight.query.ast.predicate.operator.LogicalOperator;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.QueryTokenType;
import io.github.jsoninsight.query.parser.QueryParser;
import io.github.jsoninsight.query.parser.QueryParserException;

import java.util.ArrayList;
import java.util.List;

public class QueryParserImpl implements QueryParser {

    private static class QueryParserState {
        private final List<QueryToken> tokens;
        private int pos;

        QueryParserState(List<QueryToken> tokens) {
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
                throw new QueryParserException(
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

    private ComparisonOperator toComparisonOperator(QueryToken token) {
        return switch (token.type()) {
            case EQ -> ComparisonOperator.EQ;

            case NEQ -> ComparisonOperator.NEQ;

            case GT -> ComparisonOperator.GT;

            case GTE -> ComparisonOperator.GTE;

            case LT -> ComparisonOperator.LT;

            case LTE -> ComparisonOperator.LTE;

            default -> throw new QueryParserException(
                "Expected a comparison operator",
                token,
                0
            );
        };
    }

    private QueryPredicateLiteral parseValue(QueryParserState state) {
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

            default -> throw new QueryParserException(
                "Expected a literal rightValue",
                state.current(),
                state.pos
            );
        };
    }

    private QueryPredicateArg parseArg(QueryParserState state) {
        return switch (state.current().type()) {
            case STRING -> new StringLiteralNode(state.consume().value());

            case JSON_PATH -> new JsonPathNode(state.consume().value());

            case NUMBER -> new NumberLiteralNode(new BigDecimal(state.consume().value()));

            case BOOLEAN -> new BooleanLiteralNode(
                Boolean.parseBoolean(state.consume().value())
            );

            case NULL -> {
                state.consume();
                yield new NullLiteralNode();
            }

            default -> throw new QueryParserException(
                "Expected a rightValue or pathValue argument",
                state.current(),
                state.pos
            );
        };
    }

    private QueryPredicateExpression parseFunctionCall(QueryParserState state) {
        QueryToken name = state.consume(QueryTokenType.IDENTIFIER);
        state.consume(QueryTokenType.LPAREN);

        List<QueryPredicateArg> args = new ArrayList<>();
        if (state.current().type() != QueryTokenType.RPAREN) {
            do {
                args.add(parseArg(state));
            } while (state.tryConsume(QueryTokenType.COMMA));
        }

        state.consume(QueryTokenType.RPAREN);
        return new FunctionCallNode(name.value(), args);
    }

    private QueryPredicateExpression parsePathExpression(QueryParserState state) {
        JsonPathNode path = new JsonPathNode(state.consume(QueryTokenType.JSON_PATH).value());
        QueryToken next = state.current();

        return switch (next.type()) {
            case EQ, NEQ, GT, GTE, LT, LTE -> {
                ComparisonOperator operator = toComparisonOperator(state.consume());
                yield new ComparisonNode(path, operator, parseValue(state));
            }

            case EXISTS -> {
                state.consume();
                yield new ExistsNode(path);
            }

            case IS -> {
                state.consume();
                QueryToken typeToken = state.consume(QueryTokenType.IDENTIFIER);
                JsonType jsonType;

                try {
                    jsonType = JsonType.valueOf(typeToken.value().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new QueryParserException(
                        "Unknown dataType: " + typeToken.value(),
                        typeToken,
                        state.pos
                    );
                }

                yield new IsNode(path, jsonType);
            }

            default -> throw new QueryParserException(
                "Expected operator, EXISTS or IS after pathValue",
                next,
                state.pos
            );
        };
    }

    private QueryPredicateExpression parsePrimary(QueryParserState state) {
        if (state.tryConsume(QueryTokenType.LPAREN)) {
            QueryPredicateExpression inner = parseOr(state);
            state.consume(QueryTokenType.RPAREN);
            return inner;
        }

        if (state.current().type() == QueryTokenType.IDENTIFIER) {
            return parseFunctionCall(state);
        }

        if (state.current().type() == QueryTokenType.JSON_PATH) {
            return parsePathExpression(state);
        }

        throw new QueryParserException(
            "Unexpected token " + state.current().type(),
            state.current(),
            state.pos
        );
    }

    private QueryPredicateExpression parseNot(QueryParserState state) {
        if (state.tryConsume(QueryTokenType.NOT)) {
            return new NotNode(parseNot(state));
        }

        return parsePrimary(state);
    }

    private QueryPredicateExpression parseAnd(QueryParserState state) {
        QueryPredicateExpression left = parseNot(state);

        while (state.tryConsume(QueryTokenType.AND)) {
            QueryPredicateExpression right = parseNot(state);
            left = new LogicalNode(left, LogicalOperator.AND, right);
        }

        return left;
    }

    private QueryPredicateExpression parseOr(QueryParserState state) {
        QueryPredicateExpression left = parseAnd(state);

        while (state.tryConsume(QueryTokenType.OR)) {
            QueryPredicateExpression right = parseAnd(state);
            left = new LogicalNode(left, LogicalOperator.OR, right);
        }

        return left;
    }

    @Override
    public QueryPredicateExpression parse(List<QueryToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Token list cannot be null or empty");
        }

        QueryParserState state = new QueryParserState(tokens);
        QueryPredicateExpression result = parseOr(state);

        state.consume(QueryTokenType.EOF);
        return result;
    }
}
