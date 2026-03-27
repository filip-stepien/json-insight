package io.github.jsoninsight.service.impl;

import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.service.SchemaService;

import java.util.List;
import java.util.Optional;

public class StubSchemaService implements SchemaService {

    @Override
    public JsonSchema generateSchema(String jsonContent) {
        return new JsonSchema("stub-schema", "{\"type\": \"object\"}");
    }

    @Override
    public boolean schemasMatch(JsonSchema a, JsonSchema b) {
        return false;
    }

    @Override
    public Optional<Category> categorize(JsonDocument document, List<Category> existingCategories) {
        return Optional.empty();
    }
}
