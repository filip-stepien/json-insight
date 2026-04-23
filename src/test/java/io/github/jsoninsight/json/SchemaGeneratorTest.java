package io.github.jsoninsight.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class SchemaGeneratorTest {

    @Test
    @DisplayName("Primitive types: string, integer, float, boolean, null")
    void testPrimitiveTypes() {
        assertEquals("{\"type\":\"string\"}",
                SchemaGenerator.generateSchema("\"hello\""));
        assertEquals("{\"type\":\"integer\"}",
                SchemaGenerator.generateSchema("42"));
        assertEquals("{\"type\":\"number\"}",
                SchemaGenerator.generateSchema("3.14"));
        assertEquals("{\"type\":\"boolean\"}",
                SchemaGenerator.generateSchema("true"));
        assertEquals("{\"type\":\"null\"}",
                SchemaGenerator.generateSchema("null"));
    }

    @Test
    @DisplayName("Simple object with mixed field types")
    void testSimpleObject() {
        String json = "{\"name\":\"Jan\",\"age\":30,\"active\":true}";
        String schema = SchemaGenerator.generateSchema(json);

        assertEquals(
                "{\"type\":\"object\","
                        + "\"properties\":{"
                        + "\"name\":{\"type\":\"string\"},"
                        + "\"age\":{\"type\":\"integer\"},"
                        + "\"active\":{\"type\":\"boolean\"}"
                        + "},"
                        + "\"required\":[\"active\",\"age\",\"name\"]"
                        + "}",
                schema);
    }

    @Test
    @DisplayName("Nested object")
    void testNestedObject() {
        String json = "{\"user\":{\"name\":\"Jan\",\"address\":{\"city\":\"Warszawa\"}}}";
        String schema = SchemaGenerator.generateSchema(json);

        String expected = "{\"type\":\"object\","
                + "\"properties\":{"
                + "\"user\":{\"type\":\"object\","
                + "\"properties\":{"
                + "\"name\":{\"type\":\"string\"},"
                + "\"address\":{\"type\":\"object\","
                + "\"properties\":{"
                + "\"city\":{\"type\":\"string\"}"
                + "},"
                + "\"required\":[\"city\"]"
                + "}"
                + "},"
                + "\"required\":[\"address\",\"name\"]"
                + "}"
                + "},"
                + "\"required\":[\"user\"]"
                + "}";
        assertEquals(expected, schema);
    }

    @Test
    @DisplayName("Simple array of integers")
    void testSimpleArray() {
        String schema = SchemaGenerator.generateSchema("[1,2,3]");

        assertEquals(
                "{\"type\":\"array\",\"items\":{\"type\":\"integer\"}}",
                schema);
    }

    @Test
    @DisplayName("Array of objects")
    void testArrayOfObjects() {
        String json = "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"}]";
        String schema = SchemaGenerator.generateSchema(json);

        assertEquals(
                "{\"type\":\"array\",\"items\":{"
                        + "\"type\":\"object\","
                        + "\"properties\":{"
                        + "\"id\":{\"type\":\"integer\"},"
                        + "\"name\":{\"type\":\"string\"}"
                        + "},"
                        + "\"required\":[\"id\",\"name\"]"
                        + "}}",
                schema);
    }

    @Test
    @DisplayName("Empty object and empty array")
    void testEmptyStructures() {
        assertEquals("{\"type\":\"object\",\"properties\":{},\"required\":[]}",
                SchemaGenerator.generateSchema("{}"));
        assertEquals("{\"type\":\"array\"}",
                SchemaGenerator.generateSchema("[]"));
    }

    @Test
    @DisplayName("schemasMatch returns true for structurally identical schemas")
    void testSchemasMatch() {
        String json1 = "{\"name\":\"Jan\",\"age\":30}";
        String json2 = "{\"name\":\"Anna\",\"age\":25}";

        assertTrue(SchemaGenerator.schemasMatch(json1, json2));

        String json3 = "[1,2,3]";
        String json4 = "[4,5,6]";

        assertTrue(SchemaGenerator.schemasMatch(json3, json4));
    }

    @Test
    @DisplayName("schemasMatch returns false for different schemas")
    void testSchemasDontMatch() {
        String json1 = "{\"name\":\"Jan\",\"age\":30}";
        String json2 = "{\"name\":\"Jan\",\"city\":\"Warszawa\"}";

        assertFalse(SchemaGenerator.schemasMatch(json1, json2));

        String json3 = "[1,2,3]";
        String json4 = "[\"a\",\"b\"]";

        assertFalse(SchemaGenerator.schemasMatch(json3, json4));
    }

    @Test
    @DisplayName("Invalid JSON throws exception")
    void testInvalidJson() {
        assertThrows(RuntimeException.class,
                () -> SchemaGenerator.generateSchema("{invalid}"));

        assertThrows(RuntimeException.class,
                () -> SchemaGenerator.generateSchema("[1,]"));
    }

    @Test
    @DisplayName("Negative numbers: integer and float")
    void testNegativeNumbers() {
        assertEquals("{\"type\":\"integer\"}",
                SchemaGenerator.generateSchema("-42"));
        assertEquals("{\"type\":\"number\"}",
                SchemaGenerator.generateSchema("-3.14"));
    }

    @Test
    @DisplayName("Float with exponent notation")
    void testFloatWithExponent() {
        assertEquals("{\"type\":\"number\"}",
                SchemaGenerator.generateSchema("1.23e-4"));
        assertEquals("{\"type\":\"number\"}",
                SchemaGenerator.generateSchema("1E10"));
    }

    @Test
    @DisplayName("String with escaped characters in JSON")
    void testStringWithEscapes() {
        String json = "\"hello\\nworld\"";
        String schema = SchemaGenerator.generateSchema(json);
        assertEquals("{\"type\":\"string\"}", schema);
    }

    @Test
    @DisplayName("Object with null field")
    void testObjectWithNull() {
        String json = "{\"name\":\"Jan\",\"email\":null}";
        String schema = SchemaGenerator.generateSchema(json);

        assertEquals(
                "{\"type\":\"object\","
                        + "\"properties\":{"
                        + "\"name\":{\"type\":\"string\"},"
                        + "\"email\":{\"type\":\"null\"}"
                        + "},"
                        + "\"required\":[\"email\",\"name\"]"
                        + "}",
                schema);
    }

    @Test
    @DisplayName("Array of arrays (nested arrays)")
    void testNestedArrays() {
        String json = "[[1,2],[3,4]]";
        String schema = SchemaGenerator.generateSchema(json);

        assertEquals(
                "{\"type\":\"array\",\"items\":{\"type\":\"array\",\"items\":{\"type\":\"integer\"}}}",
                schema);
    }

    @Test
    @DisplayName("Object containing array field")
    void testObjectWithArray() {
        String json = "{\"name\":\"Jan\",\"scores\":[1,2,3]}";
        String schema = SchemaGenerator.generateSchema(json);

        assertEquals(
                "{\"type\":\"object\","
                        + "\"properties\":{"
                        + "\"name\":{\"type\":\"string\"},"
                        + "\"scores\":{\"type\":\"array\",\"items\":{\"type\":\"integer\"}}"
                        + "},"
                        + "\"required\":[\"name\",\"scores\"]"
                        + "}",
                schema);
    }

    @Test
    @DisplayName("Array containing objects with nested objects")
    void testArrayOfNestedObjects() {
        String json = "[{\"user\":{\"name\":\"A\"}},{\"user\":{\"name\":\"B\"}}]";
        String schema = SchemaGenerator.generateSchema(json);

        assertEquals(
                "{\"type\":\"array\",\"items\":{"
                        + "\"type\":\"object\","
                        + "\"properties\":{"
                        + "\"user\":{\"type\":\"object\","
                        + "\"properties\":{\"name\":{\"type\":\"string\"}},"
                        + "\"required\":[\"name\"]"
                        + "}"
                        + "},"
                        + "\"required\":[\"user\"]"
                        + "}}",
                schema);
    }

    @Test
    @DisplayName("Object with single field")
    void testObjectSingleField() {
        String schema = SchemaGenerator.generateSchema("{\"key\":\"value\"}");

        assertEquals(
                "{\"type\":\"object\","
                        + "\"properties\":{\"key\":{\"type\":\"string\"}},"
                        + "\"required\":[\"key\"]"
                        + "}",
                schema);
    }

    @Test
    @DisplayName("Array with single element")
    void testArraySingleElement() {
        assertEquals(
                "{\"type\":\"array\",\"items\":{\"type\":\"string\"}}",
                SchemaGenerator.generateSchema("[\"solo\"]"));
    }

    @Test
    @DisplayName("Object with many fields")
    void testObjectWithManyFields() {
        String json = "{\"a\":1,\"b\":\"x\",\"c\":true,\"d\":null,\"e\":3.14}";
        String schema = SchemaGenerator.generateSchema(json);

        assertTrue(schema.contains("\"a\":{\"type\":\"integer\"}"));
        assertTrue(schema.contains("\"b\":{\"type\":\"string\"}"));
        assertTrue(schema.contains("\"c\":{\"type\":\"boolean\"}"));
        assertTrue(schema.contains("\"d\":{\"type\":\"null\"}"));
        assertTrue(schema.contains("\"e\":{\"type\":\"number\"}"));
        assertTrue(schema.contains("\"required\":[\"a\",\"b\",\"c\",\"d\",\"e\"]"));
    }

    @Test
    @DisplayName("Three level deep nesting")
    void testThreeLevelNesting() {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":1}}}}";
        String schema = SchemaGenerator.generateSchema(json);

        assertTrue(schema.contains("\"d\":{\"type\":\"integer\"}"));
        assertTrue(schema.startsWith("{\"type\":\"object\""));
    }

    @Test
    @DisplayName("schemasMatch: primitives match")
    void testSchemasMatchPrimitives() {
        assertTrue(SchemaGenerator.schemasMatch("\"a\"", "\"b\""));
        assertTrue(SchemaGenerator.schemasMatch("1", "999"));
        assertTrue(SchemaGenerator.schemasMatch("true", "false"));
        assertTrue(SchemaGenerator.schemasMatch("null", "null"));
    }

    @Test
    @DisplayName("schemasMatch: primitives don't match across types")
    void testSchemasDontMatchPrimitives() {
        assertFalse(SchemaGenerator.schemasMatch("\"a\"", "1"));
        assertFalse(SchemaGenerator.schemasMatch("true", "null"));
        assertFalse(SchemaGenerator.schemasMatch("1", "1.5"));
    }

    @Test
    @DisplayName("schemasMatch: different field count")
    void testSchemasDontMatchFieldCount() {
        String json1 = "{\"a\":1}";
        String json2 = "{\"a\":1,\"b\":2}";

        assertFalse(SchemaGenerator.schemasMatch(json1, json2));
    }

    @Test
    @DisplayName("schemasMatch: same structure different nesting")
    void testSchemasMatchDeepNesting() {
        String json1 = "{\"a\":{\"b\":{\"c\":1}}}";
        String json2 = "{\"a\":{\"b\":{\"c\":999}}}";

        assertTrue(SchemaGenerator.schemasMatch(json1, json2));
    }

    @Test
    @DisplayName("schemasMatch: object vs array at root")
    void testSchemasDontMatchObjectVsArray() {
        assertFalse(SchemaGenerator.schemasMatch("{\"a\":1}", "[1]"));
    }

    @Test
    @DisplayName("schemasMatch: empty structures")
    void testSchemasMatchEmpty() {
        assertTrue(SchemaGenerator.schemasMatch("{}", "{}"));
        assertTrue(SchemaGenerator.schemasMatch("[]", "[]"));
        assertFalse(SchemaGenerator.schemasMatch("{}", "[]"));
    }

    @Test
    @DisplayName("schemasMatch: array length differs but schema same")
    void testSchemasMatchArrayDifferentLength() {
        assertTrue(SchemaGenerator.schemasMatch("[1]", "[1,2,3,4,5]"));
        assertTrue(SchemaGenerator.schemasMatch("[\"a\"]", "[\"a\",\"b\",\"c\"]"));
    }

    @Test
    @DisplayName("JSON with whitespace is handled correctly")
    void testJsonWithWhitespace() {
        String json = "  {  \"name\"  :  \"Jan\"  ,  \"age\"  :  30  }  ";
        String compact = "{\"name\":\"Jan\",\"age\":30}";

        assertTrue(SchemaGenerator.schemasMatch(json, compact));
    }

    @Test
    @DisplayName("Object field order doesn't affect schema comparison")
    void testFieldOrderDoesntMatter() {
        String json1 = "{\"name\":\"Jan\",\"age\":30}";
        String json2 = "{\"age\":30,\"name\":\"Jan\"}";

        assertTrue(SchemaGenerator.schemasMatch(json1, json2));
    }

    @Test
    @DisplayName("nodeToString: string with special characters")
    void testNodeToStringStringEscaping() {
        JsonNode.StringNode node = new JsonNode.StringNode("a\"b\\c\nd");
        String result = SchemaGenerator.nodeToString(node);

        assertEquals("\"a\\\"b\\\\c\\nd\"", result);
    }

    @Test
    @DisplayName("nodeToString: number nodes")
    void testNodeToStringNumbers() {
        assertEquals("42", SchemaGenerator.nodeToString(new JsonNode.NumberNode("42", false)));
        assertEquals("3.14", SchemaGenerator.nodeToString(new JsonNode.NumberNode("3.14", true)));
        assertEquals("-7", SchemaGenerator.nodeToString(new JsonNode.NumberNode("-7", false)));
    }

    @Test
    @DisplayName("nodeToString: boolean and null")
    void testNodeToStringLiterals() {
        assertEquals("true", SchemaGenerator.nodeToString(new JsonNode.BooleanNode(true)));
        assertEquals("false", SchemaGenerator.nodeToString(new JsonNode.BooleanNode(false)));
        assertEquals("null", SchemaGenerator.nodeToString(new JsonNode.NullNode()));
    }

    @Test
    @DisplayName("nodesEqual: null node comparisons")
    void testNodesEqualNull() {
        JsonNode node = new JsonNode.StringNode("x");
        assertFalse(SchemaGenerator.nodesEqual(node, null));
        assertFalse(SchemaGenerator.nodesEqual(null, node));
        assertTrue(SchemaGenerator.nodesEqual(null, null));
    }

    @Test
    @DisplayName("nodesEqual: different types never equal")
    void testNodesEqualDifferentTypes() {
        JsonNode str = new JsonNode.StringNode("true");
        JsonNode bool = new JsonNode.BooleanNode(true);
        JsonNode num = new JsonNode.NumberNode("1", false);
        JsonNode nil = new JsonNode.NullNode();

        assertFalse(SchemaGenerator.nodesEqual(str, bool));
        assertFalse(SchemaGenerator.nodesEqual(str, num));
        assertFalse(SchemaGenerator.nodesEqual(str, nil));
        assertFalse(SchemaGenerator.nodesEqual(bool, num));
        assertFalse(SchemaGenerator.nodesEqual(bool, nil));
        assertFalse(SchemaGenerator.nodesEqual(num, nil));
    }

    @Test
    @DisplayName("nodesEqual: arrays with different lengths")
    void testNodesEqualArrayLength() {
        var a = new JsonNode.ArrayNode(java.util.List.of(new JsonNode.NumberNode("1", false)));
        var b = new JsonNode.ArrayNode(java.util.List.of(
                new JsonNode.NumberNode("1", false),
                new JsonNode.NumberNode("2", false)));

        assertFalse(SchemaGenerator.nodesEqual(a, b));
    }

    @Test
    @DisplayName("Invalid JSON: trailing comma in object")
    void testInvalidJsonTrailingCommaObject() {
        assertThrows(RuntimeException.class,
                () -> SchemaGenerator.generateSchema("{\"a\":1,}"));
    }

    @Test
    @DisplayName("Invalid JSON: standalone colon")
    void testInvalidJsonStandaloneColon() {
        assertThrows(RuntimeException.class,
                () -> SchemaGenerator.generateSchema(":"));
    }

    @Test
    @DisplayName("Invalid JSON: unmatched brace")
    void testInvalidJsonUnmatchedBrace() {
        assertThrows(RuntimeException.class,
                () -> SchemaGenerator.generateSchema("{\"a\":1"));
    }

    @Test
    @DisplayName("Invalid JSON: unmatched bracket")
    void testInvalidJsonUnmatchedBracket() {
        assertThrows(RuntimeException.class,
                () -> SchemaGenerator.generateSchema("[1,2"));
    }

    @Test
    @DisplayName("Real-world: API response shape")
    void testRealWorldApiResponse() {
        String json = "{\"status\":\"ok\",\"code\":200,\"data\":[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"name\":\"B\"}]}";
        String schema = SchemaGenerator.generateSchema(json);

        assertTrue(schema.contains("\"status\":{\"type\":\"string\"}"));
        assertTrue(schema.contains("\"code\":{\"type\":\"integer\"}"));
        assertTrue(schema.contains("\"data\":{\"type\":\"array\""));
        assertTrue(schema.contains("\"required\":[\"code\",\"data\",\"status\"]"));
    }
}
