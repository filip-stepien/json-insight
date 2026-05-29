package io.github.jsoninsight.ui.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jsoninsight.model.Collection;
import io.github.jsoninsight.model.JsonDocument;
import io.github.jsoninsight.model.JsonSchema;
import io.github.jsoninsight.model.QueryResult;
import io.github.jsoninsight.service.DocumentService;
import io.github.jsoninsight.service.QueryService;
import io.github.jsoninsight.service.SchemaService;
import io.github.jsoninsight.service.impl.DocumentServiceImpl;
import io.github.jsoninsight.service.impl.EvaluatingQueryService;
import io.github.jsoninsight.service.impl.StubSchemaService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class MainController {

    @FXML private ListView<Collection> collectionList;
    @FXML private ListView<JsonDocument> documentList;
    @FXML private TextArea documentPreview;
    @FXML private ListView<JsonDocument> searchResultsList;
    @FXML private TextField queryInput;
    @FXML private Label statusBar;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageLabel;
    @FXML private ToggleButton darkModeToggle;

    private final DocumentService documentService = new DocumentServiceImpl();
    private final SchemaService schemaService = new StubSchemaService();
    private final QueryService queryService = new EvaluatingQueryService();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final ObservableList<Collection> collections = FXCollections.observableArrayList();
    private final ObservableList<JsonDocument> documents = FXCollections.observableArrayList();
    private final ObservableList<JsonDocument> searchResults = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 20;
    private final List<JsonDocument> allResults = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    public void initialize() {
        collectionList.setItems(collections);
        collectionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Collection collection, boolean empty) {
                super.updateItem(collection, empty);
                if (empty || collection == null) {
                    setText(null);
                } else {
                    int count = documentService.getDocumentsByCollection(collection.getId()).size();
                    setText(collection.getName() + "  (" + count + ")");
                }
            }
        });
        documentList.setItems(documents);
        searchResultsList.setItems(searchResults);

        ContextMenu collectionCtxMenu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Zmień nazwę...");
        renameItem.setOnAction(e -> onRenameSelectedCollection());
        collectionCtxMenu.getItems().add(renameItem);
        collectionList.setContextMenu(collectionCtxMenu);

        collectionList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onCollectionSelected(newVal));

        collectionList.setOnMouseClicked(e -> {
            Collection selected = collectionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onCollectionSelected(selected);
            }
        });

        documentList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onDocumentSelected(newVal));

        searchResultsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onDocumentSelected(newVal));

        queryInput.setTooltip(new Tooltip(
                "Możesz wpisać sam predykat (zostanie owinięty SELECT * FROM ... WHERE)\n" +
                "lub pełne zapytanie SELECT/FROM/WHERE.\n\n" +
                "Przykłady (predykat):\n" +
                "  .age >= 18 AND .active == true\n" +
                "  .email EXISTS AND .email IS STRING\n" +
                "  size(.tags) > 1 AND matches(.email, \".+@.+\")\n\n" +
                "Pełne zapytanie:\n" +
                "  SELECT .name, .age FROM uzytkownicy WHERE .age > 21"));

        Platform.runLater(this::installSceneHandlers);

        updatePaginationUi();
        refreshData();
    }

    private void installSceneHandlers() {
        Scene scene = collectionList.getScene();
        if (scene == null) return;

        scene.setOnDragOver(e -> {
            if (e.getGestureSource() != scene && e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        scene.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean ok = false;
            if (db.hasFiles()) {
                for (File f : db.getFiles()) {
                    if (f.getName().toLowerCase(Locale.ROOT).endsWith(".json")) {
                        ingestFile(f);
                        ok = true;
                    }
                }
            }
            e.setDropCompleted(ok);
            e.consume();
        });

        KeyCombination findKey = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        scene.getAccelerators().put(findKey, this::onFindInPreview);
    }

    @FXML
    private void onAddFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        File file = fileChooser.showOpenDialog(collectionList.getScene().getWindow());
        if (file == null) return;
        ingestFile(file);
    }

    private void ingestFile(File file) {
        try {
            JsonDocument document = documentService.loadFromFile(file);

            try {
                JsonParser.parseString(document.getContent());
            } catch (Exception e) {
                showAlert("Błąd", "Plik nie zawiera poprawnego JSON:\n" + e.getMessage());
                return;
            }

            JsonSchema schema = schemaService.generateSchema(document.getContent());
            List<Collection> existingCollections = documentService.getAllCollections();
            Optional<Collection> matched = schemaService.findMatchingCollection(document, existingCollections);

            Collection collection;
            if (matched.isPresent()) {
                collection = matched.get();
            } else {
                String defaultName = "Kolekcja " + (existingCollections.size() + 1);
                String collectionName = promptForCollectionName(defaultName);
                collection = new Collection(collectionName, schema);
                documentService.addCollection(collection);
            }

            document.setCollectionId(collection.getId());
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

        File file = fileChooser.showSaveDialog(collectionList.getScene().getWindow());
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
        Collection selected = collectionList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getSchema() == null) {
            showAlert("Info", "Wybierz kolekcję, aby pobrać jej schemat.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz schemat JSON");
        fileChooser.setInitialFileName(selected.getName() + "-schema.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        File file = fileChooser.showSaveDialog(collectionList.getScene().getWindow());
        if (file == null) return;

        try {
            documentService.saveSchemaToFile(selected.getSchema(), file);
            statusBar.setText("Zapisano schemat: " + file.getName());
        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się zapisać schematu:\n" + e.getMessage());
        }
    }

    @FXML
    private void onExportResults() {
        if (allResults.isEmpty()) {
            showAlert("Info", "Brak wyników do eksportu.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksport wyników");
        fileChooser.setInitialFileName("results.jsonl");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Lines", "*.jsonl"),
                new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(collectionList.getScene().getWindow());
        if (file == null) return;

        boolean csv = file.getName().toLowerCase(Locale.ROOT).endsWith(".csv");
        try (BufferedWriter w = Files.newBufferedWriter(file.toPath())) {
            if (csv) {
                w.write("name,collectionId,content\n");
                for (JsonDocument d : allResults) {
                    w.write(csvCell(d.getName()) + "," + d.getCollectionId() + "," + csvCell(d.getContent()));
                    w.newLine();
                }
            } else {
                for (JsonDocument d : allResults) {
                    JsonObject row = new JsonObject();
                    row.addProperty("name", d.getName());
                    row.addProperty("collectionId", d.getCollectionId());
                    try {
                        row.add("content", JsonParser.parseString(d.getContent()));
                    } catch (Exception ex) {
                        row.addProperty("content", d.getContent());
                    }
                    w.write(row.toString());
                    w.newLine();
                }
            }
            statusBar.setText("Zapisano " + allResults.size() + " wyników → " + file.getName());
        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się zapisać wyników:\n" + e.getMessage());
        }
    }

    private String csvCell(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"").replace("\n", "\\n") + "\"";
    }

    @FXML
    private void onSearch() {
        String query = queryInput.getText();
        if (query == null || query.isBlank()) {
            allResults.clear();
            searchResults.clear();
            currentPage = 0;
            updatePaginationUi();
            statusBar.setText("Wpisz zapytanie.");
            return;
        }

        Optional<String> error = queryService.validateQuery(query);
        if (error.isPresent()) {
            showAlert("Błąd zapytania", error.get());
            return;
        }

        List<JsonDocument> scope = scopeForSearch();
        QueryResult result = queryService.executeQuery(query, scope);

        allResults.clear();
        allResults.addAll(result.getMatchedDocuments());
        currentPage = 0;
        applyPage();
        statusBar.setText("Znaleziono: " + result.getTotalMatches() + " z " + scope.size() + " dokumentów");
    }

    private List<JsonDocument> scopeForSearch() {
        Collection selected = collectionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return documentService.getDocumentsByCollection(selected.getId());
        }
        return documentService.getAllDocuments();
    }

    @FXML
    private void onPrevPage() {
        if (currentPage > 0) { currentPage--; applyPage(); }
    }

    @FXML
    private void onNextPage() {
        if ((currentPage + 1) * PAGE_SIZE < allResults.size()) { currentPage++; applyPage(); }
    }

    private void applyPage() {
        int from = Math.min(currentPage * PAGE_SIZE, allResults.size());
        int to = Math.min(from + PAGE_SIZE, allResults.size());
        searchResults.setAll(allResults.subList(from, to));
        updatePaginationUi();
    }

    private void updatePaginationUi() {
        int total = allResults.size();
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        pageLabel.setText("Strona " + (total == 0 ? 0 : currentPage + 1) + " / " + (total == 0 ? 0 : pages)
                + "  (" + total + ")");
        prevPageBtn.setDisable(currentPage == 0);
        nextPageBtn.setDisable((currentPage + 1) * PAGE_SIZE >= total);
    }

    @FXML
    private void onOpenQueryBuilder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/jsoninsight/ui/query-builder.fxml"));
            javafx.scene.Parent root = loader.load();
            QueryBuilderController qb = loader.getController();

            Set<String> fields = collectFieldSuggestions();
            qb.configure(queryService, fields, q -> {
                queryInput.setText(q);
                onSearch();
            });

            Stage st = new Stage();
            st.setTitle("Kreator zapytań");
            st.initModality(Modality.WINDOW_MODAL);
            st.initOwner(collectionList.getScene().getWindow());
            Scene scene = new Scene(root);
            String css = getClass().getResource("/io/github/jsoninsight/ui/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            if (darkModeToggle != null && darkModeToggle.isSelected()) {
                root.getStyleClass().add("dark");
            }
            st.setScene(scene);
            st.show();
        } catch (IOException e) {
            showAlert("Błąd", "Nie udało się otworzyć kreatora:\n" + e.getMessage());
        }
    }

    private Set<String> collectFieldSuggestions() {
        Set<String> fields = new TreeSet<>();
        List<JsonDocument> source;
        Collection selectedCol = collectionList.getSelectionModel().getSelectedItem();
        if (selectedCol != null) {
            source = documentService.getDocumentsByCollection(selectedCol.getId());
        } else {
            source = documentService.getAllDocuments();
        }
        int scanned = 0;
        for (JsonDocument d : source) {
            if (scanned++ > 25) break;
            try {
                JsonElement el = JsonParser.parseString(d.getContent());
                collectPaths(el, "", fields, 0);
            } catch (Exception ignored) {}
        }
        return fields;
    }

    private void collectPaths(JsonElement el, String prefix, Set<String> out, int depth) {
        if (depth > 4 || el == null || !el.isJsonObject()) return;
        for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
            String path = prefix + "." + e.getKey();
            out.add(path);
            if (e.getValue().isJsonObject()) collectPaths(e.getValue(), path, out, depth + 1);
        }
    }

    @FXML
    private void onToggleDarkMode() {
        Scene scene = collectionList.getScene();
        if (scene == null) return;
        if (darkModeToggle.isSelected()) {
            scene.getRoot().getStyleClass().add("dark");
        } else {
            scene.getRoot().getStyleClass().remove("dark");
        }
    }

    private void onFindInPreview() {
        if (documentPreview.getText() == null || documentPreview.getText().isEmpty()) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Znajdź w podglądzie");
        dialog.setHeaderText(null);
        dialog.setContentText("Szukaj:");
        dialog.showAndWait().ifPresent(needle -> {
            if (needle.isEmpty()) return;
            String hay = documentPreview.getText();
            int from = documentPreview.getCaretPosition();
            int idx = hay.indexOf(needle, from);
            if (idx < 0) idx = hay.indexOf(needle);
            if (idx < 0) {
                statusBar.setText("Nie znaleziono: " + needle);
            } else {
                documentPreview.selectRange(idx, idx + needle.length());
                documentPreview.requestFocus();
            }
        });
    }

    private void onCollectionSelected(Collection collection) {
        if (collection == null) {
            documents.setAll(documentService.getAllDocuments());
            return;
        }
        List<JsonDocument> filtered = documentService.getDocumentsByCollection(collection.getId());
        documents.setAll(filtered);
        documentList.getSelectionModel().clearSelection();
        showSchemaInPreview(collection);
        statusBar.setText("Kolekcja: " + collection.getName() + " (" + filtered.size() + " dokumentów)");
    }

    private String promptForCollectionName(String defaultName) {
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Nowa kolekcja");
        dialog.setHeaderText("Wykryto nowy typ dokumentu.");
        dialog.setContentText("Nadaj nazwę kolekcji:");
        return dialog.showAndWait()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(defaultName);
    }

    private void onRenameSelectedCollection() {
        Collection selected = collectionList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Wybierz kolekcję do zmiany nazwy.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Zmień nazwę kolekcji");
        dialog.setHeaderText(null);
        dialog.setContentText("Nowa nazwa:");
        dialog.showAndWait()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.equals(selected.getName()))
                .ifPresent(newName -> {
                    int id = selected.getId();
                    documentService.renameCollection(id, newName);
                    refreshData();
                    collections.stream()
                            .filter(c -> c.getId() == id)
                            .findFirst()
                            .ifPresent(c -> collectionList.getSelectionModel().select(c));
                    statusBar.setText("Zmieniono nazwę na: " + newName);
                });
    }

    private void showSchemaInPreview(Collection collection) {
        JsonSchema schema = collection.getSchema();
        if (schema == null || schema.getSchemaContent() == null) {
            documentPreview.setText("");
            return;
        }
        String raw = schema.getSchemaContent();
        try {
            documentPreview.setText(gson.toJson(JsonParser.parseString(raw)));
        } catch (Exception e) {
            documentPreview.setText(raw);
        }
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
        collections.setAll(documentService.getAllCollections());
        Collection selected = collectionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            documents.setAll(documentService.getDocumentsByCollection(selected.getId()));
        } else {
            documents.setAll(documentService.getAllDocuments());
        }
        updateStatus();
    }

    private void updateStatus() {
        int docCount = documentService.getAllDocuments().size();
        int colCount = collections.size();
        statusBar.setText(docCount + " dokumentów, " + colCount + " kolekcji");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
