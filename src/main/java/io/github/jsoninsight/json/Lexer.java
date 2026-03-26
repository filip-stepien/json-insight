package io.github.jsoninsight.json;

import java.util.ArrayList;
import java.util.List;

import static io.github.jsoninsight.json.TokenType.*;

public class Lexer {
  private final String src;
  private int pos = 0;
  private int line = 1;
  private int col = 1;

  public Lexer(String src) {
    this.src = src;
  }

  // error with location
  private RuntimeException err(String msg) {
    return new RuntimeException(msg + " [line=" + line + ", col=" + col + "]");
  }

  private char advance() {
    char c = src.charAt(pos++);
    if (c == '\n') {
      line++;
      col = 1;
    } else {
      col++;
    }
    return c;
  }

  private char current() {
    return src.charAt(pos);
  }

  private boolean hasMore() {
    return pos < src.length();
  }

  // main loop
  public List<Token> tokenize() {
    List<Token> tokens = new ArrayList<>();
    while (hasMore()) {
      skipWhitespace();
      if (!hasMore())
        break;
      int tLine = line, tCol = col;
      char c = current();
      Token t = switch (c) {
        case '{' -> {
          advance();
          yield tok(LBRACE, "{", tLine, tCol);
        }
        case '}' -> {
          advance();
          yield tok(RBRACE, "}", tLine, tCol);
        }
        case '[' -> {
          advance();
          yield tok(LBRACKET, "[", tLine, tCol);
        }
        case ']' -> {
          advance();
          yield tok(RBRACKET, "]", tLine, tCol);
        }
        case ':' -> {
          advance();
          yield tok(COLON, ":", tLine, tCol);
        }
        case ',' -> {
          advance();
          yield tok(COMMA, ",", tLine, tCol);
        }
        case '"' -> readString(tLine, tCol);
        case 't' -> readLiteral("true", BOOLEAN, tLine, tCol);
        case 'f' -> readLiteral("false", BOOLEAN, tLine, tCol);
        case 'n' -> readLiteral("null", NULL, tLine, tCol);
        default -> {
          if (c == '-' || Character.isDigit(c))
            yield readNumber(tLine, tCol);
          throw err("Unexpected character: '" + c + "'");
        }
      };
      tokens.add(t);
    }
    tokens.add(tok(EOF, "", line, col));
    return tokens;
  }

  private void skipWhitespace() {
    while (hasMore() && (current() == ' ' || current() == '\t'
        || current() == '\r' || current() == '\n'))
      advance();
  }

  // string with full unicode escape handling
  private Token readString(int tLine, int tCol) {
    advance(); // skip opening "
    StringBuilder sb = new StringBuilder();

    while (hasMore() && current() != '"') {
      char c = advance();
      if (c == '\\') {
        if (!hasMore())
          throw err("Unterminated escape sequence");
        char esc = advance();
        switch (esc) {
          case '"' -> sb.append('"');
          case '\\' -> sb.append('\\');
          case '/' -> sb.append('/');
          case 'b' -> sb.append('\b');
          case 'f' -> sb.append('\f');
          case 'n' -> sb.append('\n');
          case 'r' -> sb.append('\r');
          case 't' -> sb.append('\t');
          case 'u' -> sb.append(readUnicodeEscape(sb)); // Handle complex Unicode escapes
          default -> throw err("Invalid escape: \\" + esc);
        }
      } else if (c < 0x20) {
        // RFC 8259: unescaped control characters are illegal
        throw err("Unescaped control character: 0x" + Integer.toHexString(c));
      } else {
        sb.append(c);
      }
    }
    if (!hasMore())
      throw err("Unterminated string");
    advance(); // skip closing "
    return tok(STRING, sb.toString(), tLine, tCol);
  }

  // Parse Unicode escape sequence with surrogate pair support
  private String readUnicodeEscape(StringBuilder context) {
    int codeUnit = readHex4();

    // High surrogate? Must be followed by \\uXXXX low surrogate
    if (Character.isHighSurrogate((char) codeUnit)) {
      if (!hasMore() || current() != '\\')
        throw err("High surrogate not followed by low surrogate escape");
      advance(); // consume '\'
      if (!hasMore() || current() != 'u')
        throw err("High surrogate not followed by \\u");
      advance(); // consume 'u'
      int low = readHex4();
      if (!Character.isLowSurrogate((char) low))
        throw err("Expected low surrogate, got: " + Integer.toHexString(low));
      return new String(Character.toChars(
          Character.toCodePoint((char) codeUnit, (char) low)));
    }

    // Orphan low surrogate is technically invalid but we allow it
    return String.valueOf((char) codeUnit);
  }

  private int readHex4() {
    StringBuilder hex = new StringBuilder(4);
    for (int i = 0; i < 4; i++) {
      if (!hasMore())
        throw err("Short unicode escape");
      char h = advance();
      if (!isHex(h))
        throw err("Invalid hex digit: " + h);
      hex.append(h);
    }
    return Integer.parseInt(hex.toString(), 16);
  }

  private boolean isHex(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  // Parse a JSON number according to RFC 8259 specification
  private Token readNumber(int tLine, int tCol) {
    StringBuilder sb = new StringBuilder();
    boolean isFloat = false;

    // Optional minus
    if (current() == '-')
      sb.append(advance());

    if (!hasMore() || !Character.isDigit(current()))
      throw err("Digit expected after '-'");

    // Integer part
    if (current() == '0') {
      sb.append(advance());
      if (hasMore() && Character.isDigit(current()))
        throw err("Leading zeros are not allowed in JSON numbers");
    } else {
      while (hasMore() && Character.isDigit(current()))
        sb.append(advance());
    }

    // Fraction
    if (hasMore() && current() == '.') {
      isFloat = true;
      sb.append(advance());
      if (!hasMore() || !Character.isDigit(current()))
        throw err("Digit expected after '.'");
      while (hasMore() && Character.isDigit(current()))
        sb.append(advance());
    }

    // Exponent
    if (hasMore() && (current() == 'e' || current() == 'E')) {
      isFloat = true;
      sb.append(advance());
      if (hasMore() && (current() == '+' || current() == '-'))
        sb.append(advance());
      if (!hasMore() || !Character.isDigit(current()))
        throw err("Digit expected in exponent");
      while (hasMore() && Character.isDigit(current()))
        sb.append(advance());
    }

    return tok(isFloat ? FLOAT : INTEGER, sb.toString(), tLine, tCol);
  }

  private Token readLiteral(String word, TokenType type, int tLine, int tCol) {
    if (!src.startsWith(word, pos))
      throw err("Expected '" + word + "'");
    for (int i = 0; i < word.length(); i++)
      advance();
    // Ensure it's not part of an identifier: "trueblue" is invalid
    if (hasMore() && (Character.isLetterOrDigit(current()) || current() == '_'))
      throw err("Invalid literal near '" + word + "'");
    return tok(type, word, tLine, tCol);
  }

  private Token tok(TokenType t, String v, int ln, int col) {
    return new Token(t, v, ln, col);
  }
}