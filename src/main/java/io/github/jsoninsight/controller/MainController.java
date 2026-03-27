package io.github.jsoninsight.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.github.jsoninsight.model.Category;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.model.QueryResult;
import io.github.jsoninsight.service.DocumentService;
import io.github.jsoninsight.service.QueryService;
import io.github.jsoninsight.service.SchemaService;
import io.github.jsoninsight.service.impl.DocumentServiceImpl;
import io.github.jsoninsight.service.impl.StubQueryService;
import io.github.jsoninsight.service.impl.StubSchemaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML
    private ListView<Category> categoryList;

    @FXML
    private ListView<JsonDocument> documentList;

    @FXML
    private TextArea documentPreview;

    @FXML
    private ListView<JsonDocument> searchResultsList;

    @FXML
    private TextField queryInput;

    @FXML
    private Label statusBar;

    private final DocumentService documentService = new DocumentServiceImpl();
    private final SchemaService schemaService = new StubSchemaService();
    private final QueryService queryService = new StubQueryService();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<JsonDocument> documents = FXCollections.observableArrayList();
    private final ObservableList<JsonDocument> searchResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryList.setItems(categories);
        documentList.setItems(documents);
        searchResultsList.setItems(searchResults);

        categoryList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onCategorySelected(newVal));

        documentList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onDocumentSelected(newVal));

        searchResultsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onDocumentSelected(newVal));

        refreshData();
    }

    @FXML
    private void onAddFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        File file = fileChooser.showOpenDialog(categoryList.getScene().getWindow());
        if (file == null) return;

        try {
            JsonDocument document = documentService.loadFromFile(file);

            // Walidacja JSON
            try {
                JsonParser.parseString(document.getContent());
            } catch (Exception e) {
                showAlert("Błąd", "Plik nie zawiera poprawnego JSON:\n" + e.getMessage());
                return;
            }

            // Generowanie schematu i kategoryzacja
            JsonSchema schema = schemaService.generateSchema(document.getContent());
            List<Category> existingCategories = documentService.getAllCategories();
            Optional<Category> matchedCategory = schemaService.categorize(document, existingCategories);

            Category category;
            if (matchedCategory.isPresent()) {
                category = matchedCategory.get();
            } else {
                String categoryName = "Kategoria " + (existingCategories.size() + 1);
                category = new Category(categoryName, schema);
                documentService.addCategory(category);
            }

            document.setCategoryId(category.getId());
            documentService.addDocument(document);

            refreshData();
            statusBar.setText("Dodano: " + document.getName());

        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się wczytać pliku:\n" + e.getMessage());
        }
    }

    @FXML
    private void onExportDocument() {
        JsonDocument selected = documentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selected = searchResultsList.getSelectionModel().getSelectedItem();
        }
        if (selected == null) {
            showAlert("Info", "Wybierz dokument do pobrania.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dokument JSON");
        fileChooser.setInitialFileName(selected.getName());
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        File file = fileChooser.showSaveDialog(categoryList.getScene().getWindow());
        if (file == null) return;

        try {
            documentService.saveToFile(selected, file);
            statusBar.setText("Zapisano: " + file.getName());
        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się zapisać pliku:\n" + e.getMessage());
        }
    }

    @FXML
    private void onExportSchema() {
        Category selected = categoryList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getSchema() == null) {
            showAlert("Info", "Wybierz kategorię, aby pobrać jej schemat.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz schemat JSON");
        fileChooser.setInitialFileName(selected.getName() + "-schema.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        File file = fileChooser.showSaveDialog(categoryList.getScene().getWindow());
        if (file == null) return;

        try {
            documentService.saveSchemaToFile(selected.getSchema(), file);
            statusBar.setText("Zapisano schemat: " + file.getName());
        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się zapisać schematu:\n" + e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        String query = queryInput.getText();
        if (query == null || query.isBlank()) {
            searchResults.clear();
            statusBar.setText("Wpisz zapytanie.");
            return;
        }

        Optional<String> error = queryService.validateQuery(query);
        if (error.isPresent()) {
            showAlert("Błąd zapytania", error.get());
            return;
        }

        List<JsonDocument> allDocuments = documentService.getAllDocuments();
        QueryResult result = queryService.executeQuery(query, allDocuments);

        searchResults.setAll(result.getMatchedDocuments());
        statusBar.setText("Znaleziono: " + result.getTotalMatches() + " dokumentów");
    }

    private void onCategorySelected(Category category) {
        if (category == null) {
            documents.setAll(documentService.getAllDocuments());
            return;
        }
        List<JsonDocument> filtered = documentService.getDocumentsByCategory(category.getId());
        documents.setAll(filtered);
        statusBar.setText("Kategoria: " + category.getName() + " (" + filtered.size() + " dokumentów)");
    }

    private void onDocumentSelected(JsonDocument document) {
        if (document == null) {
            documentPreview.clear();
            return;
        }
        try {
            var jsonElement = JsonParser.parseString(document.getContent());
            documentPreview.setText(gson.toJson(jsonElement));
        } catch (Exception e) {
            documentPreview.setText(document.getContent());
        }
    }

    private void refreshData() {
        categories.setAll(documentService.getAllCategories());
        Category selected = categoryList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            documents.setAll(documentService.getDocumentsByCategory(selected.getId()));
        } else {
            documents.setAll(documentService.getAllDocuments());
        }
        updateStatus();
    }

    private void updateStatus() {
        int docCount = documentService.getAllDocuments().size();
        int catCount = categories.size();
        statusBar.setText(docCount + " dokumentów, " + catCount + " kategorii");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
