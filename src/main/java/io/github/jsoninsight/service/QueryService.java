package io.github.jsoninsight.service;

import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.QueryResult;

import java.util.List;
import java.util.Optional;

public interface QueryService {

    QueryResult executeQuery(String query, List<JsonDocument> documents);

    Optional<String> validateQuery(String query);
}
