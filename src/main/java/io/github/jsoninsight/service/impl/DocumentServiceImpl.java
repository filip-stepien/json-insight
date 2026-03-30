package io.github.jsoninsight.service.impl;

import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.service.DocumentService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentServiceImpl implements DocumentService {

    private final List<JsonDocument> documents = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final AtomicInteger documentIdSeq = new AtomicInteger(1);
    private final AtomicInteger categoryIdSeq = new AtomicInteger(1);

    @Override
    public JsonDocument loadFromFile(File file) throws IOException {
        String content = Files.readString(file.toPath());
        return new JsonDocument(file.getName(), content);
    }

    @Override
    public void saveToFile(JsonDocument document, File destination) throws IOException {
        Files.writeString(destination.toPath(), document.getContent());
    }

    @Override
    public void saveSchemaToFile(JsonSchema schema, File destination) throws IOException {
        Files.writeString(destination.toPath(), schema.getSchemaContent());
    }

    @Override
    public void addDocument(JsonDocument document) {
        document.setId(documentIdSeq.getAndIncrement());
        documents.add(document);
    }

    @Override
    public List<JsonDocument> getAllDocuments() {
        return new ArrayList<>(documents);
    }

    @Override
    public List<JsonDocument> getDocumentsByCategory(int categoryId) {
        return documents.stream()
                .filter(d -> d.getCategoryId() == categoryId)
                .toList();
    }

    @Override
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    @Override
    public void addCategory(Category category) {
        int id = categoryIdSeq.getAndIncrement();
        category.setId(id);
        category.getSchema().setId(id);
        categories.add(category);
    }
}
