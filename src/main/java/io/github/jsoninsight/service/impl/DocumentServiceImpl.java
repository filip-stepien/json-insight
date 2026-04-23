package io.github.jsoninsight.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.service.DocumentService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DocumentServiceImpl implements DocumentService {

    private static final Path DB_DIR = Path.of(System.getProperty("user.home"), ".jsoninsight");
    private static final Path DB_FILE = DB_DIR.resolve("jsoninsight.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE.toAbsolutePath();

    private final Connection connection;
    private final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    public DocumentServiceImpl() {
        this(DB_URL);
    }

    public DocumentServiceImpl(String jdbcUrl) {
        try {
            Files.createDirectories(DB_DIR);
            this.connection = DriverManager.getConnection(jdbcUrl);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            initSchema();
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Nie udało się otworzyć bazy SQLite: " + e.getMessage(), e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS schemas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    content TEXT NOT NULL
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    content TEXT NOT NULL,
                    schema_id INTEGER,
                    FOREIGN KEY (schema_id) REFERENCES schemas(id) ON DELETE SET NULL
                )
                """);
        }
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
        String raw = schema.getSchemaContent();
        String output;
        try {
            JsonElement parsed = JsonParser.parseString(raw);
            output = prettyGson.toJson(parsed);
        } catch (RuntimeException e) {
            output = raw;
        }
        Files.writeString(destination.toPath(), output);
    }

    @Override
    public void addCategory(Category category) {
        JsonSchema schema = category.getSchema();
        String content = schema.getSchemaContent() == null ? "" : schema.getSchemaContent();

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO schemas(name, content) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, content);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    category.setId(id);
                    schema.setId(id);
                    if (schema.getName() == null) {
                        schema.setName(category.getName());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Nie udało się zapisać kolekcji: " + e.getMessage(), e);
        }
    }

    @Override
    public void addDocument(JsonDocument document) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO documents(name, content, schema_id) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, document.getName());
            ps.setString(2, document.getContent());
            if (document.getCategoryId() == 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, document.getCategoryId());
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    document.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Nie udało się zapisać dokumentu: " + e.getMessage(), e);
        }
    }

    @Override
    public List<JsonDocument> getAllDocuments() {
        List<JsonDocument> out = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, name, content, schema_id FROM documents ORDER BY id")) {
            while (rs.next()) {
                out.add(readDocument(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd odczytu dokumentów: " + e.getMessage(), e);
        }
        return out;
    }

    @Override
    public List<JsonDocument> getDocumentsByCategory(int categoryId) {
        List<JsonDocument> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, name, content, schema_id FROM documents WHERE schema_id = ? ORDER BY id")) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(readDocument(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd odczytu dokumentów: " + e.getMessage(), e);
        }
        return out;
    }

    @Override
    public void renameCategory(int categoryId, String newName) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE schemas SET name = ? WHERE id = ?")) {
            ps.setString(1, newName);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Nie udało się zmienić nazwy kolekcji: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> out = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, name, content FROM schemas ORDER BY id")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String content = rs.getString("content");

                JsonSchema schema = new JsonSchema(name, content);
                schema.setId(id);

                Category cat = new Category(name, schema);
                cat.setId(id);
                out.add(cat);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd odczytu kolekcji: " + e.getMessage(), e);
        }
        return out;
    }

    private JsonDocument readDocument(ResultSet rs) throws SQLException {
        JsonDocument d = new JsonDocument();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setContent(rs.getString("content"));
        int schemaId = rs.getInt("schema_id");
        if (!rs.wasNull()) {
            d.setCategoryId(schemaId);
        }
        return d;
    }
}
