module io.github.jsoninsight {
  requires javafx.controls;
  requires javafx.fxml;
  requires static lombok;
  requires com.google.gson;
  requires java.sql;

  opens io.github.jsoninsight.ui to javafx.fxml;
  opens io.github.jsoninsight.ui.controller to javafx.fxml;

  exports io.github.jsoninsight.ui;
  exports io.github.jsoninsight.query.lexer;
  exports io.github.jsoninsight.query.lexer.impl;
  exports io.github.jsoninsight.query.parser;
  exports io.github.jsoninsight.query.parser.impl;
  exports io.github.jsoninsight.query.ast;

  exports io.github.jsoninsight.query.ast.node;
  exports io.github.jsoninsight.query.ast.operator;
  exports io.github.jsoninsight.model;
  exports io.github.jsoninsight.service;
}
