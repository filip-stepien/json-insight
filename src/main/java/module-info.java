module io.github.jsoninsight {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.jsoninsight to javafx.fxml;
    exports io.github.jsoninsight;
}