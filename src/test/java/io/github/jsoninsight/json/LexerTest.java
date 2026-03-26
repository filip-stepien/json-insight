package io.github.jsoninsight.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.jsoninsight.json.TokenType.*;

class LexerTest {

  @Test
  @DisplayName("Tokenize empty object")
  void testEmptyObject() {
    Lexer lexer = new Lexer("{}");
    List<Token> tokens = lexer.tokenize();

    assertEquals(3, tokens.size());
    assertEquals(LBRACE, tokens.get(0).type());
    assertEquals(RBRACE, tokens.get(1).type());
    assertEquals(EOF, tokens.get(2).type());
  }

  @Test
  @DisplayName("Tokenize empty array")
  void testEmptyArray() {
    Lexer lexer = new Lexer("[]");
    List<Token> tokens = lexer.tokenize();

    assertEquals(3, tokens.size());
    assertEquals(LBRACKET, tokens.get(0).type());
    assertEquals(RBRACKET, tokens.get(1).type());
    assertEquals(EOF, tokens.get(2).type());
  }

  @Test
  @DisplayName("Tokenize simple string")
  void testSimpleString() {
    Lexer lexer = new Lexer("\"hello\"");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(STRING, tokens.get(0).type());
    assertEquals("hello", tokens.get(0).value());
    assertEquals(EOF, tokens.get(1).type());
  }

  @Test
  @DisplayName("Tokenize string with escape sequences")
  void testStringWithEscapes() {
    Lexer lexer = new Lexer("\"hello\\nworld\"");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(STRING, tokens.get(0).type());
    assertEquals("hello\nworld", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize string with unicode escape")
  void testStringWithUnicode() {
    Lexer lexer = new Lexer("\"\\u0041\""); // "A"
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(STRING, tokens.get(0).type());
    assertEquals("A", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize integer")
  void testInteger() {
    Lexer lexer = new Lexer("42");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(INTEGER, tokens.get(0).type());
    assertEquals("42", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize negative integer")
  void testNegativeInteger() {
    Lexer lexer = new Lexer("-42");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(INTEGER, tokens.get(0).type());
    assertEquals("-42", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize float with decimal")
  void testFloatWithDecimal() {
    Lexer lexer = new Lexer("3.14");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(FLOAT, tokens.get(0).type());
    assertEquals("3.14", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize float with exponent")
  void testFloatWithExponent() {
    Lexer lexer = new Lexer("1.23e-4");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(FLOAT, tokens.get(0).type());
    assertEquals("1.23e-4", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize true literal")
  void testTrue() {
    Lexer lexer = new Lexer("true");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(BOOLEAN, tokens.get(0).type());
    assertEquals("true", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize false literal")
  void testFalse() {
    Lexer lexer = new Lexer("false");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(BOOLEAN, tokens.get(0).type());
    assertEquals("false", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize null literal")
  void testNull() {
    Lexer lexer = new Lexer("null");
    List<Token> tokens = lexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(NULL, tokens.get(0).type());
    assertEquals("null", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize complex object")
  void testComplexObject() {
    String json = "{\"name\": \"John\", \"age\": 30, \"active\": true}";
    Lexer lexer = new Lexer(json);
    List<Token> tokens = lexer.tokenize();

    // { "name" : "John" , "age" : 30 , "active" : true } EOF
    assertNotNull(tokens);
    assertFalse(tokens.isEmpty());
    assertEquals(LBRACE, tokens.get(0).type());

    // Find the "name" string token
    Token nameToken = tokens.stream()
        .filter(t -> t.type() == STRING && "name".equals(t.value()))
        .findFirst()
        .orElse(null);
    assertNotNull(nameToken);

    // Last token should be EOF
    assertEquals(EOF, tokens.get(tokens.size() - 1).type());
  }

  @Test
  @DisplayName("Tokenize with whitespace handling")
  void testWhitespaceHandling() {
    Lexer lexer = new Lexer("  { } ");
    List<Token> tokens = lexer.tokenize();

    assertEquals(3, tokens.size());
    assertEquals(LBRACE, tokens.get(0).type());
    assertEquals(RBRACE, tokens.get(1).type());
    assertEquals(EOF, tokens.get(2).type());
  }

  @Test
  @DisplayName("Token has correct line and column")
  void testTokenLocation() {
    Lexer lexer = new Lexer("{ \"key\" }");
    List<Token> tokens = lexer.tokenize();

    // First token should be at line 1, col 1
    Token first = tokens.get(0);
    assertEquals(1, first.line());
    assertEquals(1, first.column());

    // String token should be at line 1, col 3
    Token stringToken = tokens.get(1);
    assertEquals(1, stringToken.line());
    assertEquals(3, stringToken.column());
  }

  @Test
  @DisplayName("Error on invalid escape sequence")
  void testInvalidEscapeSequence() {
    Lexer lexer = new Lexer("\"\\x\"");
    assertThrows(RuntimeException.class, lexer::tokenize);
  }

  @Test
  @DisplayName("Error on unterminated string")
  void testUnterminatedString() {
    Lexer lexer = new Lexer("\"hello");
    assertThrows(RuntimeException.class, lexer::tokenize);
  }

  @Test
  @DisplayName("Error on leading zeros in number")
  void testLeadingZeros() {
    Lexer lexer = new Lexer("007");
    assertThrows(RuntimeException.class, lexer::tokenize);
  }

  @Test
  @DisplayName("Error on invalid literal")
  void testInvalidLiteral() {
    Lexer lexer = new Lexer("trueish");
    assertThrows(RuntimeException.class, lexer::tokenize);
  }
}
