module io.github.jsoninsight {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens io.github.jsoninsight to javafx.fxml;
    exports io.github.jsoninsight;
    exports io.github.jsoninsight.query.lexer;
    exports io.github.jsoninsight.query.lexer.impl;
}