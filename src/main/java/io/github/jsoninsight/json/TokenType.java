package io.github.jsoninsight.json;

public enum TokenType {
  LBRACE, RBRACE, // { }
  LBRACKET, RBRACKET, // [ ]
  COLON, COMMA,
  STRING, NUMBER, FLOAT, INTEGER, BOOLEAN, NULL,
  EOF
}