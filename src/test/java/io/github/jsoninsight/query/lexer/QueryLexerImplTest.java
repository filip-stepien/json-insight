package io.github.jsoninsight.query.lexer;

import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryLexerImplTest {

    private static final QueryLexerImpl lexer = new QueryLexerImpl();

    // identifiers

    @Test
    void recognizesSimpleIdentifier() {
        List<QueryToken> tokens = lexer.tokenize(".name");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(".name", tokens.getFirst().value());
    }

    @Test
    void recognizesNestedIdentifier() {
        List<QueryToken> tokens = lexer.tokenize(".address.city");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(".address.city", tokens.getFirst().value());
    }

    @Test
    void recognizesIdentifierWithUnderscore() {
        List<QueryToken> tokens = lexer.tokenize(".first_name");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(".first_name", tokens.getFirst().value());
    }

    @Test
    void recognizesIdentifierStartingWithUnderscore() {
        List<QueryToken> tokens = lexer.tokenize("._id");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals("._id", tokens.getFirst().value());
    }

    @Test
    void recognizesIdentifierNamedLikeKeyword() {
        List<QueryToken> tokens = lexer.tokenize(".and");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(".and", tokens.getFirst().value());
    }


    // keywords

    @Test
    void recognizesKeywordAnd() {
        List<QueryToken> tokens = lexer.tokenize("AND");
        assertEquals(QueryTokenType.AND, tokens.getFirst().type());
    }

    @Test
    void recognizesKeywordAndCaseInsensitive() {
        List<QueryToken> tokens = lexer.tokenize("and");
        assertEquals(QueryTokenType.AND, tokens.getFirst().type());
    }

    @Test
    void recognizesKeywordOr() {
        List<QueryToken> tokens = lexer.tokenize("OR");
        assertEquals(QueryTokenType.OR, tokens.getFirst().type());
    }

    @Test
    void recognizesKeywordNot() {
        List<QueryToken> tokens = lexer.tokenize("NOT");
        assertEquals(QueryTokenType.NOT, tokens.getFirst().type());
    }

    @Test
    void recognizesKeywordExists() {
        List<QueryToken> tokens = lexer.tokenize("EXISTS");
        assertEquals(QueryTokenType.EXISTS, tokens.getFirst().type());
    }

    @Test
    void recognizesKeywordIs() {
        List<QueryToken> tokens = lexer.tokenize("IS");
        assertEquals(QueryTokenType.IS, tokens.getFirst().type());
    }

    // literals

    @Test
    void recognizesStringLiteral() {
        List<QueryToken> tokens = lexer.tokenize("\"John\"");
        assertEquals(QueryTokenType.STRING, tokens.getFirst().type());
        assertEquals("John", tokens.getFirst().value());
    }

    @Test
    void recognizesStringWithEscapedQuote() {
        List<QueryToken> tokens = lexer.tokenize("\"say \\\"hello\\\"\"");
        assertEquals(QueryTokenType.STRING, tokens.getFirst().type());
        assertEquals("say \"hello\"", tokens.getFirst().value());
    }

    @Test
    void recognizesStringWithEscapeSequences() {
        List<QueryToken> tokens = lexer.tokenize("\"line1\\nline2\"");
        assertEquals(QueryTokenType.STRING, tokens.getFirst().type());
        assertEquals("line1\nline2", tokens.getFirst().value());
    }

    @Test
    void recognizesIntegerLiteral() {
        List<QueryToken> tokens = lexer.tokenize("42");
        assertEquals(QueryTokenType.NUMBER, tokens.getFirst().type());
        assertEquals("42", tokens.getFirst().value());
    }

    @Test
    void recognizesFloatLiteral() {
        List<QueryToken> tokens = lexer.tokenize("3.14");
        assertEquals(QueryTokenType.NUMBER, tokens.getFirst().type());
        assertEquals("3.14", tokens.getFirst().value());
    }

    @Test
    void recognizesNegativeNumber() {
        List<QueryToken> tokens = lexer.tokenize("-8");
        assertEquals(QueryTokenType.NUMBER, tokens.getFirst().type());
        assertEquals("-8", tokens.getFirst().value());
    }

    @Test
    void recognizesBooleanTrue() {
        List<QueryToken> tokens = lexer.tokenize("true");
        assertEquals(QueryTokenType.BOOLEAN, tokens.getFirst().type());
    }

    @Test
    void recognizesBooleanFalse() {
        List<QueryToken> tokens = lexer.tokenize("false");
        assertEquals(QueryTokenType.BOOLEAN, tokens.getFirst().type());
    }

    @Test
    void recognizesBooleanCaseInsensitive() {
        List<QueryToken> tokens = lexer.tokenize("TRUE");
        assertEquals(QueryTokenType.BOOLEAN, tokens.getFirst().type());
    }

    @Test
    void recognizesNullLiteral() {
        List<QueryToken> tokens = lexer.tokenize("null");
        assertEquals(QueryTokenType.NULL, tokens.getFirst().type());
    }

    // operators

    @Test
    void recognizesEqualityOperator() {
        List<QueryToken> tokens = lexer.tokenize("==");
        assertEquals(QueryTokenType.EQ, tokens.getFirst().type());
    }

    @Test
    void recognizesNotEqualOperator() {
        List<QueryToken> tokens = lexer.tokenize("!=");
        assertEquals(QueryTokenType.NEQ, tokens.getFirst().type());
    }

    @Test
    void recognizesGreaterThanOperator() {
        List<QueryToken> tokens = lexer.tokenize(">");
        assertEquals(QueryTokenType.GT, tokens.getFirst().type());
    }

    @Test
    void recognizesGreaterThanOrEqualOperator() {
        List<QueryToken> tokens = lexer.tokenize(">=");
        assertEquals(QueryTokenType.GTE, tokens.getFirst().type());
    }

    @Test
    void recognizesLessThanOperator() {
        List<QueryToken> tokens = lexer.tokenize("<");
        assertEquals(QueryTokenType.LT, tokens.getFirst().type());
    }

    @Test
    void recognizesLessThanOrEqualOperator() {
        List<QueryToken> tokens = lexer.tokenize("<=");
        assertEquals(QueryTokenType.LTE, tokens.getFirst().type());
    }

    // punctuation

    @Test
    void recognizesLeftParen() {
        List<QueryToken> tokens = lexer.tokenize("(");
        assertEquals(QueryTokenType.LPAREN, tokens.getFirst().type());
    }

    @Test
    void recognizesRightParen() {
        List<QueryToken> tokens = lexer.tokenize(")");
        assertEquals(QueryTokenType.RPAREN, tokens.getFirst().type());
    }

    @Test
    void recognizesComma() {
        List<QueryToken> tokens = lexer.tokenize(",");
        assertEquals(QueryTokenType.COMMA, tokens.getFirst().type());
    }

    // EOF

    @Test
    void alwaysEndsWithEof() {
        List<QueryToken> tokens = lexer.tokenize(".name");
        assertEquals(QueryTokenType.EOF, tokens.getLast().type());
    }

    @Test
    void returnsOnlyEofForEmptyInput() {
        List<QueryToken> tokens = lexer.tokenize("");
        assertEquals(1, tokens.size());
        assertEquals(QueryTokenType.EOF, tokens.getFirst().type());
    }

    @Test
    void returnsOnlyEofForWhitespaceInput() {
        List<QueryToken> tokens = lexer.tokenize("   ");
        assertEquals(1, tokens.size());
        assertEquals(QueryTokenType.EOF, tokens.getFirst().type());
    }

    // full expressions

    @Test
    void tokenizesComparisonExpression() {
        List<QueryToken> tokens = lexer.tokenize(".age == 25");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(QueryTokenType.EQ, tokens.get(1).type());
        assertEquals(QueryTokenType.NUMBER, tokens.get(2).type());
        assertEquals(QueryTokenType.EOF, tokens.get(3).type());
    }

    @Test
    void tokenizesExistsExpression() {
        List<QueryToken> tokens = lexer.tokenize(".name EXISTS");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(QueryTokenType.EXISTS, tokens.get(1).type());
    }

    @Test
    void tokenizesIsExpression() {
        List<QueryToken> tokens = lexer.tokenize(".age IS NUMBER");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(QueryTokenType.IS, tokens.get(1).type());
        assertEquals(QueryTokenType.IDENTIFIER, tokens.get(2).type());
        assertEquals("NUMBER", tokens.get(2).value());
    }

    @Test
    void tokenizesAndExpression() {
        List<QueryToken> tokens = lexer.tokenize(".name == \"John\" AND .age > 25");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(QueryTokenType.EQ, tokens.get(1).type());
        assertEquals(QueryTokenType.STRING, tokens.get(2).type());
        assertEquals(QueryTokenType.AND, tokens.get(3).type());
        assertEquals(QueryTokenType.IDENTIFIER, tokens.get(4).type());
        assertEquals(QueryTokenType.GT, tokens.get(5).type());
        assertEquals(QueryTokenType.NUMBER, tokens.get(6).type());
    }

    @Test
    void tokenizesFunctionCall() {
        List<QueryToken> tokens = lexer.tokenize("contains(.tags, \"admin\")");
        assertEquals(QueryTokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals(QueryTokenType.LPAREN, tokens.get(1).type());
        assertEquals(QueryTokenType.IDENTIFIER, tokens.get(2).type());
        assertEquals(QueryTokenType.COMMA, tokens.get(3).type());
        assertEquals(QueryTokenType.STRING, tokens.get(4).type());
        assertEquals(QueryTokenType.RPAREN, tokens.get(5).type());
    }

    // exceptions

    @Test
    void throwsIllegalArgumentExceptionForNullInput() {
        assertThrows(IllegalArgumentException.class, () -> lexer.tokenize(null));
    }

    @Test
    void throwsForUnknownCharacter() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("@name"));
    }

    @Test
    void throwsForSingleEquals() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("="));
    }

    @Test
    void throwsForSingleExclamationMark() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("!"));
    }

    @Test
    void throwsForUnterminatedString() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("\"unterminated"));
    }

    @Test
    void throwsForUnterminatedEscapeSequence() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("\"test\\"));
    }

    @Test
    void throwsForUnsupportedEscapeSequence() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("\"\\q\""));
    }

    @Test
    void throwsForIdentifierEndingWithDot() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize(".name."));
    }

    @Test
    void throwsForIdentifierWithDoubleDot() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize(".address..city"));
    }

    @Test
    void throwsForNumberWithMultipleDecimalPoints() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("3.1.4"));
    }

    @Test
    void throwsForNumberWithTrailingDecimalPoint() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("3."));
    }

    @Test
    void throwsForInvalidNegativeNumber() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("- 5"));
    }

    @Test
    void throwsForNegativeNumberFollowedByLetter() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("-abc"));
    }

    @Test
    void throwsForNegativeNumberWithTrailingDecimalPoint() {
        assertThrows(QueryLexerException.class, () -> lexer.tokenize("-3."));
    }

    @Test
    void exceptionContainsPositionOfFailedCharacter() {
        QueryLexerException exception = assertThrows(
            QueryLexerException.class,
            () -> lexer.tokenize(".name @field")
        );
        assertEquals(6, exception.getPosition());
    }

    @Test
    void exceptionContainsFailedCharacter() {
        QueryLexerException exception = assertThrows(
            QueryLexerException.class,
            () -> lexer.tokenize("@name")
        );
        assertEquals('@', exception.getCharacter());
    }
}
