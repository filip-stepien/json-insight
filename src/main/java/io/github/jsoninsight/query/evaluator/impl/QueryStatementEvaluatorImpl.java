package io.github.jsoninsight.query.evaluator.impl;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.ast.predicate.node.JsonPathNode;
import io.github.jsoninsight.query.ast.statement.QueryStatement;
import io.github.jsoninsight.query.ast.statement.clause.FromClause;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.ast.statement.clause.WhereClause;
import io.github.jsoninsight.query.evaluator.QueryPredicateEvaluator;
import io.github.jsoninsight.query.evaluator.QueryStatementEvaluator;
import io.github.jsoninsight.query.evaluator.QueryStatementEvaluatorException;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class QueryStatementEvaluatorImpl implements QueryStatementEvaluator {

    private final QueryPredicateEvaluator predicateEvaluator;

    private static String toFieldKey(String pathValue) {
        String withoutLeadingDot = pathValue.substring(1);
        int lastDot = withoutLeadingDot.lastIndexOf('.');
        return lastDot == -1 ? withoutLeadingDot : withoutLeadingDot.substring(lastDot + 1);
    }

    private static Optional<JsonNode> resolveJsonPath(JsonNode document, JsonPathNode path) {
        String[] segments = path.pathValue().substring(1).split("\\.");
        JsonNode resolvedNode = document;

        for (String segment : segments) {
            if (!(resolvedNode instanceof JsonNode.ObjectNode obj)) {
                return Optional.empty();
            }

            JsonNode fieldNode = obj.fields().get(segment);

            if (fieldNode == null) {
                return Optional.empty();
            }

            resolvedNode = fieldNode;
        }
        return Optional.of(resolvedNode);
    }

    private JsonNode projectDocumentFields(JsonNode document, List<JsonPathNode> paths) {
        Map<String, JsonNode> result = new LinkedHashMap<>();

        for (JsonPathNode path : paths) {
            resolveJsonPath(document, path).ifPresent(value ->
                result.put(toFieldKey(path.pathValue()), value)
            );
        }

        return new JsonNode.ObjectNode(result);
    }

    private List<JsonNode> applySelectClause(List<JsonNode> documents, SelectClause select) {
        return switch (select) {
            case SelectClause.Wildcard ignored -> documents;
            case SelectClause.Fields fields -> documents.stream()
                .map(doc -> projectDocumentFields(doc, fields.paths()))
                .toList();
        };
    }

    private List<JsonNode> applyWhereClause(List<JsonNode> documents, Optional<WhereClause> where) {
        if (where.isEmpty()) {
            return documents;
        }

        QueryPredicateExpression predicate = where.get().predicate();
        return documents.stream()
            .filter(doc -> predicateEvaluator.evaluate(doc, predicate))
            .toList();
    }

    private List<JsonNode> getCollectionDocuments(FromClause from, Map<String, List<JsonNode>> collections) {
        List<JsonNode> documents = collections.get(from.collectionName());

        if (documents == null) {
            throw new QueryStatementEvaluatorException("Collection not found: " + from.collectionName());
        }

        return documents;
    }

    @Override
    public List<JsonNode> evaluate(QueryStatement statement, Map<String, List<JsonNode>> collections) {
        List<JsonNode> documents = getCollectionDocuments(statement.from(), collections);
        List<JsonNode> filtered = applyWhereClause(documents, statement.where());
        return applySelectClause(filtered, statement.select());
    }
}
