package io.github.jsoninsight.service.impl;

import io.github.jsoninsight.json.SchemaGenerator;
import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.service.SchemaService;

import java.util.List;
import java.util.Optional;

public class StubSchemaService implements SchemaService {

    @Override
    public JsonSchema generateSchema(String jsonContent) {
        String content = SchemaGenerator.generateSchema(jsonContent);
        return new JsonSchema("schema", content);
    }

    @Override
    public boolean schemasMatch(JsonSchema a, JsonSchema b) {
        if (a == null || b == null) return false;
        String left = a.getSchemaContent();
        String right = b.getSchemaContent();
        if (left == null || right == null) return false;
        return left.equals(right);
    }

    @Override
    public Optional<Category> categorize(JsonDocument document, List<Category> existingCategories) {
        if (document == null || existingCategories == null || existingCategories.isEmpty()) {
            return Optional.empty();
        }
        JsonSchema docSchema = generateSchema(document.getContent());
        return existingCategories.stream()
                .filter(cat -> cat.getSchema() != null && schemasMatch(docSchema, cat.getSchema()))
                .findFirst();
    }
}
