package io.github.jsoninsight.model;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private int id;
    private String name;
    private JsonSchema schema;
    private List<JsonDocument> documents;

    public Category() {
        this.documents = new ArrayList<>();
    }

    public Category(String name, JsonSchema schema) {
        this.name = name;
        this.schema = schema;
        this.documents = new ArrayList<>();
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

    public JsonSchema getSchema() {
        return schema;
    }

    public void setSchema(JsonSchema schema) {
        this.schema = schema;
    }

    public List<JsonDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<JsonDocument> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        return name;
    }
}
