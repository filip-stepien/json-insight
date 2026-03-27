module io.github.jsoninsight {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;
    requires com.google.gson;

    opens io.github.jsoninsight to javafx.fxml;
    opens io.github.jsoninsight.controller to javafx.fxml;
    exports io.github.jsoninsight;
    exports io.github.jsoninsight.query.lexer;
    exports io.github.jsoninsight.query.lexer.impl;
    exports io.github.jsoninsight.model;
    exports io.github.jsoninsight.service;
}
