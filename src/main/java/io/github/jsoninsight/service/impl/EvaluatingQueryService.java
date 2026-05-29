package io.github.jsoninsight.service.impl;

import io.github.jsoninsight.json.JsonLexer;
import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.json.JsonParser;
import io.github.jsoninsight.json.Token;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.QueryResult;
import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.expression.node.JsonPathNode;
import io.github.jsoninsight.query.ast.statement.QueryStatement;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.evaluator.QueryEvaluatorException;
import io.github.jsoninsight.query.evaluator.QueryExpressionEvaluator;
import io.github.jsoninsight.query.evaluator.impl.QueryExpressionEvaluatorImpl;
import io.github.jsoninsight.query.lexer.QueryLexer;
import io.github.jsoninsight.query.lexer.QueryLexerException;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.QueryParserException;
import io.github.jsoninsight.query.parser.QueryParserFactory;
import io.github.jsoninsight.query.parser.QueryStatementParser;
import io.github.jsoninsight.service.QueryService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluatingQueryService implements QueryService {

    private static final String AUTO_COLLECTION = "auto";

    private final QueryLexer queryLexer = new QueryLexerImpl();
    private final QueryStatementParser statementParser = QueryParserFactory.createStatementParser();
    private final QueryExpressionEvaluator expressionEvaluator = new QueryExpressionEvaluatorImpl();

    private String wrapIfBare(String query) {
        String trimmed = query.trim();
        if (trimmed.regionMatches(true, 0, "SELECT", 0, 6)) {
            return trimmed;
        }
        return "SELECT * FROM " + AUTO_COLLECTION + " WHERE " + trimmed;
    }

    @Override
    public Optional<String> validateQuery(String query) {
        if (query == null || query.isBlank()) {
            return Optional.of("Zapytanie jest puste.");
        }
        try {
            List<QueryToken> tokens = queryLexer.tokenize(wrapIfBare(query));
            statementParser.parse(tokens);
            return Optional.empty();
        } catch (QueryLexerException e) {
            return Optional.of("Lexer: " + e.getMessage());
        } catch (QueryParserException e) {
            return Optional.of("Parser: " + e.getMessage() + " (pozycja " + e.getPosition() + ")");
        } catch (RuntimeException e) {
            return Optional.of(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    @Override
    public QueryResult executeQuery(String query, List<JsonDocument> documents) {
        QueryStatement statement;
        try {
            List<QueryToken> tokens = queryLexer.tokenize(wrapIfBare(query));
            statement = statementParser.parse(tokens);
        } catch (RuntimeException e) {
            return new QueryResult(List.of(), query);
        }

        Optional<QueryExpression> whereExpression = statement.where().map(w -> w.expression());
        SelectClause select = statement.select();

        List<JsonDocument> matched = new ArrayList<>();
        int projectionIndex = 0;

        for (JsonDocument doc : documents) {
            JsonNode parsed = parseJsonContent(doc.getContent());
            if (parsed == null) continue;

            if (whereExpression.isPresent() && !matchesWhere(parsed, whereExpression.get())) {
                continue;
            }

            switch (select) {
                case SelectClause.Wildcard ignored -> matched.add(doc);
                case SelectClause.Fields fields -> {
                    JsonNode projected = project(parsed, fields.paths());
                    matched.add(new JsonDocument("projekcja-" + (++projectionIndex), serialize(projected)));
                }
            }
        }
        return new QueryResult(matched, query);
    }

    private boolean matchesWhere(JsonNode document, QueryExpression where) {
        try {
            return expressionEvaluator.evaluate(document, where).asBoolean();
        } catch (QueryEvaluatorException | UnsupportedOperationException e) {
            return false;
        }
    }

    private JsonNode project(JsonNode document, List<JsonPathNode> paths) {
        Map<String, JsonNode> result = new LinkedHashMap<>();
        for (JsonPathNode path : paths) {
            resolvePath(document, path).ifPresent(value ->
                    result.put(lastSegment(path.pathValue()), value)
            );
        }
        return new JsonNode.ObjectNode(result);
    }

    private Optional<JsonNode> resolvePath(JsonNode document, JsonPathNode path) {
        String raw = path.pathValue();
        String trimmed = raw.startsWith(".") ? raw.substring(1) : raw;
        if (trimmed.isEmpty()) return Optional.of(document);
        JsonNode current = document;
        for (String segment : trimmed.split("\\.")) {
            if (!(current instanceof JsonNode.ObjectNode obj)) return Optional.empty();
            JsonNode next = obj.fields().get(segment);
            if (next == null) return Optional.empty();
            current = next;
        }
        return Optional.of(current);
    }

    private String lastSegment(String pathValue) {
        String trimmed = pathValue.startsWith(".") ? pathValue.substring(1) : pathValue;
        int lastDot = trimmed.lastIndexOf('.');
        return lastDot == -1 ? trimmed : trimmed.substring(lastDot + 1);
    }

    private JsonNode parseJsonContent(String content) {
        try {
            List<Token> tokens = new JsonLexer(content).tokenize();
            return new JsonParser(tokens).parse();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String serialize(JsonNode node) {
        return switch (node) {
            case JsonNode.StringNode(String value) -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            case JsonNode.NumberNode num -> num.raw();
            case JsonNode.BooleanNode(boolean value) -> Boolean.toString(value);
            case JsonNode.NullNode ignored -> "null";
            case JsonNode.ArrayNode arr -> arr.elements().stream()
                    .map(this::serialize)
                    .collect(Collectors.joining(",", "[", "]"));
            case JsonNode.ObjectNode obj -> obj.fields().entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\":" + serialize(e.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        };
    }
}
