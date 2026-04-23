package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.statement.QueryStatement;

import java.util.List;
import java.util.Map;

public interface QueryStatementEvaluator {
    List<JsonNode> evaluate(QueryStatement statement, Map<String, List<JsonNode>> collections);
}