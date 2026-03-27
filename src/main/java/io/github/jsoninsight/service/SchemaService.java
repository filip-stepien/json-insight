package io.github.jsoninsight.service;

import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;

import java.util.List;
import java.util.Optional;

public interface SchemaService {

    JsonSchema generateSchema(String jsonContent);

    boolean schemasMatch(JsonSchema a, JsonSchema b);

    Optional<Category> categorize(JsonDocument document, List<Category> existingCategories);
}
