package io.github.jsoninsight.service.impl;

import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.service.DocumentService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DocumentServiceImpl implements DocumentService {

    private final DatabaseManager db;

    public DocumentServiceImpl() {
        this.db = DatabaseManager.getInstance();
    }

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
        String sql = "INSERT INTO documents (name, content, category_id, added_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, document.getName());
            stmt.setString(2, document.getContent());
            stmt.setInt(3, document.getCategoryId());
            stmt.setString(4, document.getAddedAt().toString());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    document.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd zapisu dokumentu", e);
        }
    }

    @Override
    public List<JsonDocument> getAllDocuments() {
        String sql = "SELECT id, name, content, category_id, added_at FROM documents ORDER BY added_at DESC";
        List<JsonDocument> documents = new ArrayList<>();

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                documents.add(mapDocument(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd odczytu dokumentów", e);
        }
        return documents;
    }

    @Override
    public List<JsonDocument> getDocumentsByCategory(int categoryId) {
        String sql = "SELECT id, name, content, category_id, added_at FROM documents WHERE category_id = ? ORDER BY added_at DESC";
        List<JsonDocument> documents = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(mapDocument(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd odczytu dokumentów kategorii", e);
        }
        return documents;
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT id, name, schema_content, created_at FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonSchema schema = new JsonSchema();
                schema.setId(rs.getInt("id"));
                schema.setName(rs.getString("name"));
                schema.setSchemaContent(rs.getString("schema_content"));
                schema.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));

                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setSchema(schema);

                categories.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd odczytu kategorii", e);
        }
        return categories;
    }

    @Override
    public void addCategory(Category category) {
        String sql = "INSERT INTO categories (name, schema_content, created_at) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getSchema().getSchemaContent());
            stmt.setString(3, category.getSchema().getCreatedAt().toString());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    category.setId(id);
                    category.getSchema().setId(id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd zapisu kategorii", e);
        }
    }

    private JsonDocument mapDocument(ResultSet rs) throws SQLException {
        JsonDocument doc = new JsonDocument();
        doc.setId(rs.getInt("id"));
        doc.setName(rs.getString("name"));
        doc.setContent(rs.getString("content"));
        doc.setCategoryId(rs.getInt("category_id"));
        doc.setAddedAt(LocalDateTime.parse(rs.getString("added_at")));
        return doc;
    }
}
