package io.github.jsoninsight.service;

import io.github.jsoninsight.model.Collection;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DocumentService {

    JsonDocument loadFromFile(File file) throws IOException;

    void saveToFile(JsonDocument document, File destination) throws IOException;

    void saveSchemaToFile(JsonSchema schema, File destination) throws IOException;

    void addDocument(JsonDocument document);

    List<JsonDocument> getAllDocuments();

    List<JsonDocument> getDocumentsByCollection(int collectionId);

    List<Collection> getAllCollections();

    void addCollection(Collection collection);

    void renameCollection(int collectionId, String newName);
}
