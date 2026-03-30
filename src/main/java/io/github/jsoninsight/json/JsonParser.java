package io.github.jsoninsight.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.jsoninsight.json.TokenType.*;

public class JsonParser {

    private final List<Token> tokens;
    private int pos = 0;


    private Token current() {
        return tokens.get(pos);
    }

    @SuppressWarnings("UnusedReturnValue")
    private Token advance() {
        return tokens.get(pos++);
    }

    public JsonParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public JsonNode parse() {
        JsonNode node = parseValue();
        if (current().type() != EOF) {
            throw err("Unexpected token after JSON value");
        }
        return node;
    }

    private JsonNode parseValue() {
        Token t = current();
        return switch (t.type()) {
            case LBRACE -> parseObject();
            case LBRACKET -> parseArray();
            case STRING -> {
                advance();
                yield new JsonNode.StringNode(t.value());
            }
            case INTEGER, FLOAT, NUMBER -> {
                advance();
                yield new JsonNode.NumberNode(t.value(), t.type() == FLOAT);
            }
            case BOOLEAN -> {
                advance();
                yield new JsonNode.BooleanNode(Boolean.parseBoolean(t.value()));
            }
            case NULL -> {
                advance();
                yield new JsonNode.NullNode();
            }
            default -> throw err("Unexpected token: " + t.type());
        };
    }

    private JsonNode parseObject() {
        expect(LBRACE);
        Map<String, JsonNode> fields = new LinkedHashMap<>();

        if (current().type() == RBRACE) {
            advance();
            return new JsonNode.ObjectNode(fields);
        }

        while (true) {
            Token key = expect(STRING);
            expect(COLON);
            JsonNode value = parseValue();
            fields.put(key.value(), value);

            if (current().type() == COMMA) {
                advance();
            } else {
                break;
            }
        }

        expect(RBRACE);
        return new JsonNode.ObjectNode(fields);
    }

    private JsonNode parseArray() {
        expect(LBRACKET);
        List<JsonNode> elements = new ArrayList<>();

        // empty array
        if (current().type() == RBRACKET) {
            advance();
            return new JsonNode.ArrayNode(elements);
        }

        while (true) {
            elements.add(parseValue());

            if (current().type() == COMMA) {
                advance();
            } else {
                break;
            }
        }

        expect(RBRACKET);
        return new JsonNode.ArrayNode(elements);
    }

    private Token expect(TokenType type) {
        Token t = current();
        if (t.type() != type) {
            throw err("Expected " + type + " but got " + t.type());
        }
        advance();
        return t;
    }

    private RuntimeException err(String msg) {
        Token t = current();
        return new RuntimeException(msg + " [line=" + t.line() + ", col=" + t.column() + "]");
    }
}
