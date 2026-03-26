package io.github.jsoninsight.query.lexer.impl;

import io.github.jsoninsight.query.lexer.QueryLexer;
import io.github.jsoninsight.query.lexer.QueryLexerException;
import io.github.jsoninsight.query.lexer.QueryToken;
import io.github.jsoninsight.query.lexer.QueryTokenType;
import java.util.ArrayList;
import java.util.List;

public class QueryLexerImpl implements QueryLexer {

    private static class QueryLexerState {
        final String input;
        int pos;

        QueryLexerState(String input) {
            this.input = input;
            this.pos = 0;
        }

        char current() {
            return input.charAt(pos);
        }

        char peekNext() {
            return pos + 1 < input.length() ? input.charAt(pos + 1) : 0;
        }

        boolean hasMore() {
            return pos < input.length();
        }

        boolean isAtEnd() {
            return pos + 1 >= input.length();
        }
    }

    private boolean isIdentifierStart(char character) {
        return Character.isLetter(character) || character == '_';
    }

    private boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }

    private void skipWhitespace(QueryLexerState state) {
        while (state.hasMore() && Character.isWhitespace(state.current())) {
            state.pos++;
        }
    }

    private QueryToken readWord(QueryLexerState state) {
        int start = state.pos;

        if (!isIdentifierStart(state.current())) {
            throw new QueryLexerException(
                "Invalid identifier start",
                state.current(),
                state.pos
            );
        }

        state.pos++;

        while (state.hasMore()) {
            char currentChar = state.current();

            if (isIdentifierPart(currentChar)) {
                state.pos++;
                continue;
            }

            if (currentChar == '.') {
                if (state.isAtEnd()) {
                    throw new QueryLexerException(
                        "Identifier cannot end with '.'",
                        currentChar,
                        state.pos
                    );
                }

                if (!isIdentifierStart(state.peekNext())) {
                    throw new QueryLexerException(
                        "Dot in identifier must be followed by a valid identifier segment",
                        currentChar,
                        state.pos
                    );
                }

                state.pos++; // consume '.'
                continue;
            }

            break;
        }

        String word = state.input.substring(start, state.pos);

        return switch (word.toUpperCase()) {
            case "AND" ->
                new QueryToken(QueryTokenType.AND, word);

            case "OR" ->
                new QueryToken(QueryTokenType.OR, word);

            case "NOT" ->
                new QueryToken(QueryTokenType.NOT, word);

            case "EXISTS" ->
                new QueryToken(QueryTokenType.EXISTS, word);

            case "IS" ->
                new QueryToken(QueryTokenType.IS, word);

            case "TRUE", "FALSE" ->
                new QueryToken(QueryTokenType.BOOLEAN, word);

            case "NULL" ->
                new QueryToken(QueryTokenType.NULL, word);

            default ->
                new QueryToken(QueryTokenType.IDENTIFIER, word);
        };
    }

    private QueryToken readNumber(QueryLexerState state) {
        int start = state.pos;
        boolean hasDot = false;

        if (state.current() == '-') {
            state.pos++;
        }

        while (state.hasMore()) {
            char currentChar = state.current();

            if (Character.isDigit(currentChar)) {
                state.pos++;
                continue;
            }

            if (currentChar == '.') {
                if (hasDot) {
                    throw new QueryLexerException(
                        "Invalid number format: multiple decimal points",
                        currentChar,
                        state.pos
                    );
                }

                if (!Character.isDigit(state.peekNext())) {
                    throw new QueryLexerException(
                        "Invalid number format: decimal point must be followed by a digit",
                        currentChar,
                        state.pos
                    );
                }

                hasDot = true;
                state.pos++;
                continue;
            }

            break;
        }

        return new QueryToken(QueryTokenType.NUMBER, state.input.substring(start, state.pos));
    }

    private QueryToken readString(QueryLexerState state) {
        int openingQuotePos = state.pos;
        state.pos++; // skip opening "

        StringBuilder value = new StringBuilder();

        while (state.hasMore()) {
            char currentChar = state.current();

            if (currentChar == '"') {
                state.pos++; // skip closing "
                return new QueryToken(QueryTokenType.STRING, value.toString());
            }

            if (currentChar == '\\') {
                if (state.isAtEnd()) {
                    throw new QueryLexerException(
                        "Unterminated escape sequence in string",
                        currentChar,
                        state.pos
                    );
                }

                char escaped = state.peekNext();

                switch (escaped) {
                    case '"'  -> value.append('"');
                    case '\\' -> value.append('\\');
                    case 'n'  -> value.append('\n');
                    case 't'  -> value.append('\t');
                    case 'r'  -> value.append('\r');
                    case 'b'  -> value.append('\b');
                    case 'f'  -> value.append('\f');
                    default -> throw new QueryLexerException(
                        "Unsupported escape sequence",
                        escaped,
                        state.pos + 1
                    );
                }

                state.pos += 2;
                continue;
            }

            value.append(currentChar);
            state.pos++;
        }

        throw new QueryLexerException("Unterminated string literal", '"', openingQuotePos);
    }

    private QueryToken readOperator(QueryLexerState state) {
        char currentChar = state.current();
        char nextChar = state.peekNext();

        switch (currentChar) {
            case '=':
                if (nextChar == '=') {
                    state.pos += 2;
                    return new QueryToken(QueryTokenType.EQ, "==");
                }
                throw new QueryLexerException("Expected '=='", currentChar, state.pos);

            case '!':
                if (nextChar == '=') {
                    state.pos += 2;
                    return new QueryToken(QueryTokenType.NEQ, "!=");
                }
                throw new QueryLexerException("Expected '!='", currentChar, state.pos);

            case '>':
                if (nextChar == '=') {
                    state.pos += 2;
                    return new QueryToken(QueryTokenType.GTE, ">=");
                }
                state.pos++;
                return new QueryToken(QueryTokenType.GT, ">");

            case '<':
                if (nextChar == '=') {
                    state.pos += 2;
                    return new QueryToken(QueryTokenType.LTE, "<=");
                }
                state.pos++;
                return new QueryToken(QueryTokenType.LT, "<");

            case '(':
                state.pos++;
                return new QueryToken(QueryTokenType.LPAREN, "(");

            case ')':
                state.pos++;
                return new QueryToken(QueryTokenType.RPAREN, ")");

            case ',':
                state.pos++;
                return new QueryToken(QueryTokenType.COMMA, ",");

            default:
                throw new QueryLexerException("Unknown character", currentChar, state.pos);
        }
    }

    private QueryToken readNextToken(QueryLexerState state) {
        char currentChar = state.current();

        if (isIdentifierStart(currentChar)) {
            return readWord(state);
        }

        if (Character.isDigit(currentChar)) {
            return readNumber(state);
        }

        if (currentChar == '-' && Character.isDigit(state.peekNext())) {
            return readNumber(state);
        }

        if (currentChar == '"') {
            return readString(state);
        }

        return readOperator(state);
    }

    @Override
    public List<QueryToken> tokenize(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        QueryLexerState state = new QueryLexerState(input);
        List<QueryToken> tokens = new ArrayList<>();

        while (state.hasMore()) {
            skipWhitespace(state);

            if (state.hasMore()) {
                tokens.add(readNextToken(state));
            }
        }

        tokens.add(new QueryToken(QueryTokenType.EOF, ""));
        return tokens;
    }
}
