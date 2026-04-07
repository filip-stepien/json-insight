package io.github.jsoninsight.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.jsoninsight.json.TokenType.*;

class JsonLexerTest {

  @Test
  @DisplayName("Tokenize empty object")
  void testEmptyObject() {
    JsonLexer jsonLexer = new JsonLexer("{}");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(3, tokens.size());
    assertEquals(LBRACE, tokens.get(0).type());
    assertEquals(RBRACE, tokens.get(1).type());
    assertEquals(EOF, tokens.get(2).type());
  }

  @Test
  @DisplayName("Tokenize empty array")
  void testEmptyArray() {
    JsonLexer jsonLexer = new JsonLexer("[]");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(3, tokens.size());
    assertEquals(LBRACKET, tokens.get(0).type());
    assertEquals(RBRACKET, tokens.get(1).type());
    assertEquals(EOF, tokens.get(2).type());
  }

  @Test
  @DisplayName("Tokenize simple string")
  void testSimpleString() {
    JsonLexer jsonLexer = new JsonLexer("\"hello\"");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(STRING, tokens.get(0).type());
    assertEquals("hello", tokens.get(0).value());
    assertEquals(EOF, tokens.get(1).type());
  }

  @Test
  @DisplayName("Tokenize string with escape sequences")
  void testStringWithEscapes() {
    JsonLexer jsonLexer = new JsonLexer("\"hello\\nworld\"");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(STRING, tokens.get(0).type());
    assertEquals("hello\nworld", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize string with unicode escape")
  void testStringWithUnicode() {
    JsonLexer jsonLexer = new JsonLexer("\"\\u0041\""); // "A"
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(STRING, tokens.get(0).type());
    assertEquals("A", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize integer")
  void testInteger() {
    JsonLexer jsonLexer = new JsonLexer("42");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(INTEGER, tokens.get(0).type());
    assertEquals("42", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize negative integer")
  void testNegativeInteger() {
    JsonLexer jsonLexer = new JsonLexer("-42");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(INTEGER, tokens.get(0).type());
    assertEquals("-42", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize float with decimal")
  void testFloatWithDecimal() {
    JsonLexer jsonLexer = new JsonLexer("3.14");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(FLOAT, tokens.get(0).type());
    assertEquals("3.14", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize float with exponent")
  void testFloatWithExponent() {
    JsonLexer jsonLexer = new JsonLexer("1.23e-4");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(FLOAT, tokens.get(0).type());
    assertEquals("1.23e-4", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize true literal")
  void testTrue() {
    JsonLexer jsonLexer = new JsonLexer("true");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(BOOLEAN, tokens.get(0).type());
    assertEquals("true", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize false literal")
  void testFalse() {
    JsonLexer jsonLexer = new JsonLexer("false");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(BOOLEAN, tokens.get(0).type());
    assertEquals("false", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize null literal")
  void testNull() {
    JsonLexer jsonLexer = new JsonLexer("null");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(2, tokens.size());
    assertEquals(NULL, tokens.get(0).type());
    assertEquals("null", tokens.get(0).value());
  }

  @Test
  @DisplayName("Tokenize complex object")
  void testComplexObject() {
    String json = "{\"name\": \"John\", \"age\": 30, \"active\": true}";
    JsonLexer jsonLexer = new JsonLexer(json);
    List<Token> tokens = jsonLexer.tokenize();

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
    JsonLexer jsonLexer = new JsonLexer("  { } ");
    List<Token> tokens = jsonLexer.tokenize();

    assertEquals(3, tokens.size());
    assertEquals(LBRACE, tokens.get(0).type());
    assertEquals(RBRACE, tokens.get(1).type());
    assertEquals(EOF, tokens.get(2).type());
  }

  @Test
  @DisplayName("Token has correct line and column")
  void testTokenLocation() {
    JsonLexer jsonLexer = new JsonLexer("{ \"key\" }");
    List<Token> tokens = jsonLexer.tokenize();

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
    JsonLexer jsonLexer = new JsonLexer("\"\\x\"");
    assertThrows(RuntimeException.class, jsonLexer::tokenize);
  }

  @Test
  @DisplayName("Error on unterminated string")
  void testUnterminatedString() {
    JsonLexer jsonLexer = new JsonLexer("\"hello");
    assertThrows(RuntimeException.class, jsonLexer::tokenize);
  }

  @Test
  @DisplayName("Error on leading zeros in number")
  void testLeadingZeros() {
    JsonLexer jsonLexer = new JsonLexer("007");
    assertThrows(RuntimeException.class, jsonLexer::tokenize);
  }

  @Test
  @DisplayName("Error on invalid literal")
  void testInvalidLiteral() {
    JsonLexer jsonLexer = new JsonLexer("trueish");
    assertThrows(RuntimeException.class, jsonLexer::tokenize);
  }
}
