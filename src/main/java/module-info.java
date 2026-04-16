module io.github.jsoninsight {
  requires javafx.controls;
  requires javafx.fxml;
  requires static lombok;
  requires com.google.gson;

  opens io.github.jsoninsight.ui to javafx.fxml;
  opens io.github.jsoninsight.ui.controller to javafx.fxml;

  exports io.github.jsoninsight.ui;
  exports io.github.jsoninsight.query.lexer;
  exports io.github.jsoninsight.query.lexer.impl;
  exports io.github.jsoninsight.query.parser;
  exports io.github.jsoninsight.query.parser.impl;
  exports io.github.jsoninsight.query.ast.predicate;
  exports io.github.jsoninsight.query.ast.predicate.node;
  exports io.github.jsoninsight.query.ast.predicate.operator;
  exports io.github.jsoninsight.query.ast.statement;
  exports io.github.jsoninsight.query.ast.statement.clause;
  exports io.github.jsoninsight.query.evaluator;
  exports io.github.jsoninsight.query.evaluator.impl;
  exports io.github.jsoninsight.model;
  exports io.github.jsoninsight.service;
}
