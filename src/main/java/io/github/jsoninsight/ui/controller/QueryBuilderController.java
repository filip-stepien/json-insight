package io.github.jsoninsight.ui.controller;

import io.github.jsoninsight.service.QueryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class QueryBuilderController {

    @FXML private VBox treeContainer;
    @FXML private TextArea previewArea;
    @FXML private Label builderStatus;

    private QueryService queryService;
    private Consumer<String> onApplyCallback;
    private final List<String> suggestedFields = new ArrayList<>();

    private final QbGroup root = new QbGroup();

    @FXML
    public void initialize() {
        if (root.children.isEmpty()) {
            root.children.add(new QbRule());
        }
        rebuild();
    }

    public void configure(QueryService qs, Collection<String> fields, Consumer<String> onApply) {
        this.queryService = qs;
        this.onApplyCallback = onApply;
        this.suggestedFields.clear();
        if (fields != null) this.suggestedFields.addAll(fields);
        rebuild();
    }

    private void rebuild() {
        treeContainer.getChildren().setAll(renderGroup(root, true, 0));
        refreshPreview();
    }

    private void refreshPreview() {
        previewArea.setText(serialize(root));
    }

    private Node renderGroup(QbGroup g, boolean isRoot, int depth) {
        VBox box = new VBox(6);
        box.getStyleClass().add(depth == 0 ? "qb-group" : "qb-group-nested");

        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("qb-group-header");

        ToggleButton notTog = new ToggleButton("NOT");
        notTog.setSelected(g.negated);
        notTog.setOnAction(e -> { g.negated = notTog.isSelected(); rebuild(); });

        ChoiceBox<Combinator> combo = new ChoiceBox<>(FXCollections.observableArrayList(Combinator.values()));
        combo.setValue(g.combinator);
        combo.setOnAction(e -> {
            Combinator v = combo.getValue();
            if (v != null && v != g.combinator) {
                g.combinator = v;
                rebuild();
            }
        });

        Button addRule = new Button("+ Warunek");
        addRule.setOnAction(e -> { g.children.add(new QbRule()); rebuild(); });

        Button addGroup = new Button("+ Grupa");
        addGroup.setOnAction(e -> {
            QbGroup sub = new QbGroup();
            sub.children.add(new QbRule());
            g.children.add(sub);
            rebuild();
        });

        header.getChildren().addAll(notTog, combo, addRule, addGroup);

        if (!isRoot) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button rm = new Button("×");
            rm.getStyleClass().add("qb-remove");
            rm.setOnAction(e -> {
                if (removeFromTree(g, root)) rebuild();
            });
            header.getChildren().addAll(spacer, rm);
        }

        box.getChildren().add(header);

        VBox childrenBox = new VBox(4);
        childrenBox.setPadding(new Insets(0, 0, 0, 16));
        for (QbNode child : g.children) {
            Node node = switch (child) {
                case QbRule r -> renderRule(r, g);
                case QbGroup sub -> renderGroup(sub, false, depth + 1);
            };
            childrenBox.getChildren().add(node);
        }
        box.getChildren().add(childrenBox);

        return box;
    }

    private Node renderRule(QbRule r, QbGroup parent) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("qb-rule");

        ComboBox<String> field = new ComboBox<>(FXCollections.observableArrayList(suggestedFields));
        field.setEditable(true);
        field.setPromptText(".pole");
        field.setPrefWidth(200);
        field.getEditor().setText(r.field);
        field.getEditor().textProperty().addListener((o, ov, nv) -> {
            r.field = nv == null ? "" : nv;
            refreshPreview();
        });

        ChoiceBox<String> op = new ChoiceBox<>(FXCollections.observableArrayList(allOperators()));
        op.setPrefWidth(140);
        op.setValue(r.operator);
        op.setOnAction(e -> {
            String v = op.getValue();
            if (v != null && !Objects.equals(v, r.operator)) {
                r.operator = v;
                rebuild();
            }
        });

        Region valueField;
        if (operatorTakesValue(r.operator)) {
            TextField val = new TextField(r.value);
            val.setPromptText(valuePromptFor(r.operator));
            val.textProperty().addListener((o, ov, nv) -> {
                r.value = nv == null ? "" : nv;
                refreshPreview();
            });
            HBox.setHgrow(val, Priority.ALWAYS);
            valueField = val;
        } else {
            Region empty = new Region();
            HBox.setHgrow(empty, Priority.ALWAYS);
            valueField = empty;
        }

        Button rm = new Button("×");
        rm.getStyleClass().add("qb-remove");
        rm.setOnAction(e -> { parent.children.remove(r); rebuild(); });

        row.getChildren().addAll(field, op, valueField, rm);
        return row;
    }

    private static List<String> allOperators() {
        return List.of(
                "==", "!=", ">", ">=", "<", "<=",
                "EXISTS", "NOT EXISTS",
                "IS STRING", "IS NUMBER", "IS BOOLEAN", "IS NULL", "IS ARRAY", "IS OBJECT",
                "matches",
                "size ==", "size !=", "size >", "size >=", "size <", "size <="
        );
    }

    private static boolean operatorTakesValue(String op) {
        return !"EXISTS".equals(op) && !"NOT EXISTS".equals(op) && !op.startsWith("IS ");
    }

    private static String valuePromptFor(String op) {
        if ("matches".equals(op)) return "regex, np. .+@.+";
        if (op.startsWith("size")) return "liczba";
        return "wartość";
    }

    private boolean removeFromTree(QbGroup target, QbGroup current) {
        if (current.children.remove(target)) return true;
        for (QbNode child : current.children) {
            if (child instanceof QbGroup g && removeFromTree(target, g)) return true;
        }
        return false;
    }

    private String serialize(QbGroup g) {
        List<String> parts = new ArrayList<>();
        for (QbNode child : g.children) {
            String s = switch (child) {
                case QbRule r -> serializeRule(r);
                case QbGroup sub -> serialize(sub);
            };
            if (s != null && !s.isBlank()) parts.add(s);
        }
        if (parts.isEmpty()) return "";

        String body = parts.size() == 1
                ? parts.get(0)
                : "(" + String.join(" " + g.combinator.name() + " ", parts) + ")";

        return g.negated ? "NOT " + (body.startsWith("(") ? body : "(" + body + ")") : body;
    }

    private String serializeRule(QbRule r) {
        String field = r.field == null ? "" : r.field.trim();
        String op = r.operator;
        String value = r.value == null ? "" : r.value.trim();

        if (field.isBlank()) return "";
        if (!field.startsWith(".")) field = "." + field;

        if (op.startsWith("IS ")) {
            return field + " " + op;
        }
        return switch (op) {
            case "EXISTS" -> field + " EXISTS";
            case "NOT EXISTS" -> "NOT (" + field + " EXISTS)";
            case "matches" -> "matches(" + field + ", " + quote(value) + ")";
            case "size ==", "size !=", "size >", "size >=", "size <", "size <=" -> {
                String compare = op.substring("size ".length());
                yield "size(" + field + ") " + compare + " " + bareNumber(value);
            }
            default -> field + " " + op + " " + quote(value);
        };
    }

    private String quote(String raw) {
        if (raw.isEmpty()) return "\"\"";
        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false") || raw.equalsIgnoreCase("null")) return raw;
        try { Double.parseDouble(raw); return raw; } catch (NumberFormatException ignored) {}
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) return raw;
        return "\"" + raw.replace("\"", "\\\"") + "\"";
    }

    private String bareNumber(String raw) {
        if (raw.isEmpty()) return "0";
        return raw;
    }

    @FXML
    private void onValidate() {
        if (queryService == null) { setStatus("Brak serwisu zapytań."); return; }
        String q = previewArea.getText();
        if (q == null || q.isBlank()) { setStatus("Zapytanie jest puste."); return; }
        Optional<String> err = queryService.validateQuery(q);
        setStatus(err.map(s -> "Błąd: " + s).orElse("OK ✓"));
    }

    @FXML
    private void onApply() {
        String q = previewArea.getText();
        if (q == null || q.isBlank()) { setStatus("Zapytanie jest puste."); return; }
        if (queryService != null) {
            Optional<String> err = queryService.validateQuery(q);
            if (err.isPresent()) { setStatus("Błąd: " + err.get()); return; }
        }
        if (onApplyCallback != null) onApplyCallback.accept(q.trim());
        close();
    }

    @FXML
    private void onCancel() { close(); }

    private void close() {
        Stage st = (Stage) treeContainer.getScene().getWindow();
        st.close();
    }

    private void setStatus(String s) { builderStatus.setText(s); }

    enum Combinator { AND, OR }

    sealed interface QbNode permits QbRule, QbGroup {}

    static final class QbRule implements QbNode {
        String field = "";
        String operator = "==";
        String value = "";
    }

    static final class QbGroup implements QbNode {
        Combinator combinator = Combinator.AND;
        boolean negated = false;
        final List<QbNode> children = new ArrayList<>();
    }
}
