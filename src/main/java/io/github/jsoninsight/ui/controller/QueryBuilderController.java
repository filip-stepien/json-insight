package io.github.jsoninsight.ui.controller;

import io.github.jsoninsight.query.ast.operator.ComparisonOperator;
import io.github.jsoninsight.query.ast.operator.JsonType;
import io.github.jsoninsight.service.QueryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class QueryBuilderController {

    @FXML private ComboBox<String> fieldCombo;
    @FXML private ComboBox<String> operatorCombo;
    @FXML private TextField valueInput;
    @FXML private TextArea queryArea;
    @FXML private Label builderStatus;

    private final Deque<String> history = new ArrayDeque<>();
    private QueryService queryService;
    private Consumer<String> onApplyCallback;

    private static String symbolOf(ComparisonOperator op) {
        switch (op) {
            case EQ:  return "==";
            case NEQ: return "!=";
            case GT:  return ">";
            case GTE: return ">=";
            case LT:  return "<";
            case LTE: return "<=";
            default:  return op.name();
        }
    }

    private static List<String> buildOperatorList() {
        List<String> ops = new ArrayList<>();
        for (ComparisonOperator op : ComparisonOperator.values()) ops.add(symbolOf(op));
        ops.add("EXISTS");
        ops.add("NOT EXISTS");
        for (JsonType t : JsonType.values()) ops.add("IS " + t.name());
        ops.add("contains(...)");
        ops.add("size(...)");
        ops.add("startsWith(...)");
        ops.add("matches(...)");
        return ops;
    }

    @FXML
    public void initialize() {
        operatorCombo.setItems(FXCollections.observableArrayList(buildOperatorList()));
        operatorCombo.getSelectionModel().select(0);
    }

    public void configure(QueryService queryService,
                          Collection<String> suggestedFields,
                          Consumer<String> onApply) {
        this.queryService = queryService;
        this.onApplyCallback = onApply;
        if (suggestedFields != null && !suggestedFields.isEmpty()) {
            fieldCombo.setItems(FXCollections.observableArrayList(suggestedFields));
        }
    }

    @FXML
    private void onAddCondition() {
        String field = textOf(fieldCombo.getEditor().getText());
        String op = operatorCombo.getValue();
        String value = textOf(valueInput.getText());

        if (field.isBlank() || op == null) {
            setStatus("Podaj pole i operator.");
            return;
        }

        // Lexer Filipa wymaga, żeby ścieżka JSON zaczynała się od kropki.
        if (!field.startsWith(".")) {
            field = "." + field;
        }

        String condition = buildCondition(field, op, value);
        if (condition == null) return;
        appendToken(condition);
    }

    private String buildCondition(String field, String op, String value) {
        switch (op) {
            case "EXISTS":
                return field + " EXISTS";
            case "NOT EXISTS":
                return field + " NOT EXISTS";
            case "IS STRING": case "IS NUMBER": case "IS BOOLEAN":
            case "IS ARRAY":  case "IS OBJECT": case "IS NULL":
                return field + " " + op;
            case "contains(...)":
                if (value.isBlank()) { setStatus("contains wymaga wartości."); return null; }
                return "contains(" + field + ", " + quoteIfNeeded(value) + ")";
            case "size(...)":
                if (value.isBlank()) { setStatus("size wymaga porównania, np. > 3."); return null; }
                return "size(" + field + ") " + value;
            case "startsWith(...)":
                if (value.isBlank()) { setStatus("startsWith wymaga prefiksu."); return null; }
                return "startsWith(" + field + ", " + quoteIfNeeded(value) + ")";
            case "matches(...)":
                if (value.isBlank()) { setStatus("matches wymaga wyrażenia regex."); return null; }
                return "matches(" + field + ", " + quoteIfNeeded(value) + ")";
            default:
                if (value.isBlank()) { setStatus("Podaj wartość."); return null; }
                return field + " " + op + " " + quoteIfNeeded(value);
        }
    }

    private String quoteIfNeeded(String raw) {
        String v = raw.trim();
        if (v.isEmpty()) return "\"\"";
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false") || v.equalsIgnoreCase("null")) return v;
        try { Double.parseDouble(v); return v; } catch (NumberFormatException ignored) {}
        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) return v;
        return "\"" + v.replace("\"", "\\\"") + "\"";
    }

    @FXML private void onAnd()        { appendToken("AND"); }
    @FXML private void onOr()         { appendToken("OR"); }
    @FXML private void onNot()        { appendToken("NOT"); }
    @FXML private void onOpenParen()  { appendRaw("("); }
    @FXML private void onCloseParen() { appendRaw(")"); }

    @FXML
    private void onUndo() {
        if (history.isEmpty()) return;
        queryArea.setText(history.pop());
        setStatus("Cofnięto.");
    }

    @FXML
    private void onClear() {
        pushHistory();
        queryArea.clear();
        setStatus("Wyczyszczono.");
    }

    @FXML
    private void onValidate() {
        if (queryService == null) { setStatus("Brak serwisu zapytań."); return; }
        String q = queryArea.getText();
        if (q == null || q.isBlank()) { setStatus("Zapytanie jest puste."); return; }
        Optional<String> err = queryService.validateQuery(q);
        setStatus(err.map(s -> "Błąd: " + s).orElse("OK ✓"));
    }

    @FXML
    private void onApply() {
        String q = queryArea.getText();
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
        Stage st = (Stage) queryArea.getScene().getWindow();
        st.close();
    }

    private void appendToken(String token) {
        pushHistory();
        String cur = textOf(queryArea.getText());
        if (cur.isEmpty() || cur.endsWith("(")) {
            queryArea.setText(cur + token);
        } else {
            queryArea.setText(cur + " " + token);
        }
        setStatus("");
    }

    private void appendRaw(String raw) {
        pushHistory();
        String cur = textOf(queryArea.getText());
        if (raw.equals("(")) {
            queryArea.setText(cur.isEmpty() || cur.endsWith("(") ? cur + raw : cur + " " + raw);
        } else {
            queryArea.setText(cur + raw);
        }
    }

    private void pushHistory() {
        history.push(textOf(queryArea.getText()));
        if (history.size() > 50) history.pollLast();
    }

    private void setStatus(String s) { builderStatus.setText(s); }

    private static String textOf(String s) { return s == null ? "" : s; }
}
