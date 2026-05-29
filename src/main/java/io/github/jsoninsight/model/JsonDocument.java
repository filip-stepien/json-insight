package io.github.jsoninsight.model;

import java.time.LocalDateTime;

public class JsonDocument {

    private int id;
    private String name;
    private String content;
    private int collectionId;
    private LocalDateTime addedAt;

    public JsonDocument() {
    }

    public JsonDocument(String name, String content) {
        this.name = name;
        this.content = content;
        this.addedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
