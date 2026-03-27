package io.github.jsoninsight.service.impl;

import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.QueryResult;
import io.github.jsoninsight.service.QueryService;

import java.util.List;
import java.util.Optional;

public class StubQueryService implements QueryService {

    @Override
    public QueryResult executeQuery(String query, List<JsonDocument> documents) {
        return new QueryResult(documents, query);
    }

    @Override
    public Optional<String> validateQuery(String query) {
        return Optional.empty();
    }
}
