package io.github.jsoninsight.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

    private JsonNode parse(String json) {
        Lexer lexer = new Lexer(json);
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        return parser.parse();
    }

    @Test
    @DisplayName("Parse empty object")
    void testEmptyObject() {
        JsonNode node = parse("{}");
        assertInstanceOf(JsonNode.ObjectNode.class, node);
        JsonNode.ObjectNode obj = (JsonNode.ObjectNode) node;
        assertTrue(obj.fields().isEmpty());
    }

    @Test
    @DisplayName("Parse empty array")
    void testEmptyArray() {
        JsonNode node = parse("[]");
        assertInstanceOf(JsonNode.ArrayNode.class, node);
        JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
        assertTrue(arr.elements().isEmpty());
    }

    @Test
    @DisplayName("Parse string value")
    void testStringValue() {
        JsonNode node = parse("\"hello\"");
        assertInstanceOf(JsonNode.StringNode.class, node);
        assertEquals("hello", ((JsonNode.StringNode) node).value());
    }

    @Test
    @DisplayName("Parse integer value")
    void testIntegerValue() {
        JsonNode node = parse("42");
        assertInstanceOf(JsonNode.NumberNode.class, node);
        JsonNode.NumberNode num = (JsonNode.NumberNode) node;
        assertEquals("42", num.raw());
        assertFalse(num.isFloat());
    }

    @Test
    @DisplayName("Parse negative integer value")
    void testNegativeIntegerValue() {
        JsonNode node = parse("-42");
        assertInstanceOf(JsonNode.NumberNode.class, node);
        JsonNode.NumberNode num = (JsonNode.NumberNode) node;
        assertEquals("-42", num.raw());
        assertFalse(num.isFloat());
    }

    @Test
    @DisplayName("Parse float value")
    void testFloatValue() {
        JsonNode node = parse("3.14");
        assertInstanceOf(JsonNode.NumberNode.class, node);
        JsonNode.NumberNode num = (JsonNode.NumberNode) node;
        assertEquals("3.14", num.raw());
        assertTrue(num.isFloat());
    }

    @Test
    @DisplayName("Parse float with exponent")
    void testFloatWithExponent() {
        JsonNode node = parse("1.23e-4");
        assertInstanceOf(JsonNode.NumberNode.class, node);
        JsonNode.NumberNode num = (JsonNode.NumberNode) node;
        assertEquals("1.23e-4", num.raw());
        assertTrue(num.isFloat());
    }

    @Test
    @DisplayName("Parse true boolean")
    void testTrueValue() {
        JsonNode node = parse("true");
        assertInstanceOf(JsonNode.BooleanNode.class, node);
        assertTrue(((JsonNode.BooleanNode) node).value());
    }

    @Test
    @DisplayName("Parse false boolean")
    void testFalseValue() {
        JsonNode node = parse("false");
        assertInstanceOf(JsonNode.BooleanNode.class, node);
        assertFalse(((JsonNode.BooleanNode) node).value());
    }

    @Test
    @DisplayName("Parse null value")
    void testNullValue() {
        JsonNode node = parse("null");
        assertInstanceOf(JsonNode.NullNode.class, node);
    }

    @Test
    @DisplayName("Parse object with single field")
    void testObjectSingleField() {
        JsonNode node = parse("{\"name\": \"John\"}");
        assertInstanceOf(JsonNode.ObjectNode.class, node);
        JsonNode.ObjectNode obj = (JsonNode.ObjectNode) node;
        assertEquals(1, obj.fields().size());
        assertInstanceOf(JsonNode.StringNode.class, obj.fields().get("name"));
        assertEquals("John", ((JsonNode.StringNode) obj.fields().get("name")).value());
    }

    @Test
    @DisplayName("Parse object with multiple fields")
    void testObjectMultipleFields() {
        JsonNode node = parse("{\"name\": \"John\", \"age\": 30}");
        assertInstanceOf(JsonNode.ObjectNode.class, node);
        JsonNode.ObjectNode obj = (JsonNode.ObjectNode) node;
        assertEquals(2, obj.fields().size());
        assertEquals("John", ((JsonNode.StringNode) obj.fields().get("name")).value());
        assertEquals("30", ((JsonNode.NumberNode) obj.fields().get("age")).raw());
    }

    @Test
    @DisplayName("Parse array with single element")
    void testArraySingleElement() {
        JsonNode node = parse("[\"a\"]");
        assertInstanceOf(JsonNode.ArrayNode.class, node);
        JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
        assertEquals(1, arr.elements().size());
        assertEquals("a", ((JsonNode.StringNode) arr.elements().getFirst()).value());
    }

    @Test
    @DisplayName("Parse array with multiple elements")
    void testArrayMultipleElements() {
        JsonNode node = parse("[1, 2, 3]");
        assertInstanceOf(JsonNode.ArrayNode.class, node);
        JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
        assertEquals(3, arr.elements().size());
        assertEquals("1", ((JsonNode.NumberNode) arr.elements().get(0)).raw());
        assertEquals("2", ((JsonNode.NumberNode) arr.elements().get(1)).raw());
        assertEquals("3", ((JsonNode.NumberNode) arr.elements().get(2)).raw());
    }

    @Test
    @DisplayName("Parse array with mixed values")
    void testArrayMixedValues() {
        JsonNode node = parse("[\"text\", 42, true, null]");
        assertInstanceOf(JsonNode.ArrayNode.class, node);
        JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
        assertEquals(4, arr.elements().size());
        assertInstanceOf(JsonNode.StringNode.class, arr.elements().get(0));
        assertInstanceOf(JsonNode.NumberNode.class, arr.elements().get(1));
        assertInstanceOf(JsonNode.BooleanNode.class, arr.elements().get(2));
        assertInstanceOf(JsonNode.NullNode.class, arr.elements().get(3));
    }

    @Test
    @DisplayName("Parse nested object")
    void testNestedObject() {
        JsonNode node = parse("{\"outer\": {\"inner\": \"value\"}}");
        assertInstanceOf(JsonNode.ObjectNode.class, node);
        JsonNode.ObjectNode outer = (JsonNode.ObjectNode) node;
        JsonNode inner = outer.fields().get("outer");
        assertInstanceOf(JsonNode.ObjectNode.class, inner);
        JsonNode.ObjectNode innerObj = (JsonNode.ObjectNode) inner;
        assertEquals("value", ((JsonNode.StringNode) innerObj.fields().get("inner")).value());
    }

    @Test
    @DisplayName("Parse nested array")
    void testNestedArray() {
        JsonNode node = parse("[[1, 2], [3, 4]]");
        assertInstanceOf(JsonNode.ArrayNode.class, node);
        JsonNode.ArrayNode outer = (JsonNode.ArrayNode) node;
        assertEquals(2, outer.elements().size());
        JsonNode.ArrayNode arr1 = (JsonNode.ArrayNode) outer.elements().getFirst();
        assertEquals(2, arr1.elements().size());
        assertEquals("1", ((JsonNode.NumberNode) arr1.elements().getFirst()).raw());
    }

    @Test
    @DisplayName("Parse complex nested structure")
    void testComplexNestedStructure() {
        JsonNode node = parse("{\"array\": [{\"name\": \"a\"}, {\"name\": \"b\"}], \"count\": 2}");
        assertInstanceOf(JsonNode.ObjectNode.class, node);
        JsonNode.ObjectNode obj = (JsonNode.ObjectNode) node;
        JsonNode.ArrayNode arr = (JsonNode.ArrayNode) obj.fields().get("array");
        assertEquals(2, arr.elements().size());
        assertEquals("a", ((JsonNode.StringNode) ((JsonNode.ObjectNode) arr.elements().getFirst()).fields().get("name")).value());
        assertEquals("2", ((JsonNode.NumberNode) obj.fields().get("count")).raw());
    }

    @Test
    @DisplayName("Error on unexpected token after JSON value")
    void testUnexpectedTokenAfterValue() {
        Lexer lexer = new Lexer("{},");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("Unexpected token after JSON value"));
    }

    @Test
    @DisplayName("Error on missing colon in object")
    void testMissingColon() {
        Lexer lexer = new Lexer("{\"key\" \"value\"}");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("Expected COLON"));
    }

    @Test
    @DisplayName("Error on missing comma in object")
    void testMissingCommaInObject() {
        Lexer lexer = new Lexer("{\"a\": 1 \"b\": 2}");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("Expected RBRACE"));
    }

    @Test
    @DisplayName("Error on missing comma in array")
    void testMissingCommaInArray() {
        Lexer lexer = new Lexer("[1 2]");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("Expected RBRACKET"));
    }

    @Test
    @DisplayName("Error on unclosed object")
    void testUnclosedObject() {
        Lexer lexer = new Lexer("{\"key\": \"value\" ");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("Expected RBRACE"));
    }

    @Test
    @DisplayName("Error on unclosed array")
    void testUnclosedArray() {
        Lexer lexer = new Lexer("[1, 2 ");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("Expected RBRACKET"));
    }

    @Test
    @DisplayName("Error message contains line and column")
    void testErrorMessageContainsLocation() {
        Lexer lexer = new Lexer("{ \"key\" }");
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        RuntimeException ex = assertThrows(RuntimeException.class, parser::parse);
        assertTrue(ex.getMessage().contains("line="));
        assertTrue(ex.getMessage().contains("col="));
    }
}