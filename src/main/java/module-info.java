module io.github.jsoninsight {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens io.github.jsoninsight to javafx.fxml;
    exports io.github.jsoninsight;
}