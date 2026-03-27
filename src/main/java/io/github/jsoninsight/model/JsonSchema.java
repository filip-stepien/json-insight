package io.github.jsoninsight.model;

import java.time.LocalDateTime;

public class JsonSchema {

    private int id;
    private String name;
    private String schemaContent;
    private LocalDateTime createdAt;

    public JsonSchema() {
    }

    public JsonSchema(String name, String schemaContent) {
        this.name = name;
        this.schemaContent = schemaContent;
        this.createdAt = LocalDateTime.now();
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

    public String getSchemaContent() {
        return schemaContent;
    }

    public void setSchemaContent(String schemaContent) {
        this.schemaContent = schemaContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
