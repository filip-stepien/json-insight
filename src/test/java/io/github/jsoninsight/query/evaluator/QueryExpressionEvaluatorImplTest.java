package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.json.JsonParser;
import io.github.jsoninsight.json.JsonLexer;
import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.evaluator.impl.QueryExpressionEvaluatorImpl;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.impl.QueryExpressionParserImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryExpressionEvaluatorImplTest {

    private static final QueryLexerImpl queryLexer = new QueryLexerImpl();
    private static final QueryExpressionParserImpl queryParser = new QueryExpressionParserImpl();
    private static final QueryExpressionEvaluator evaluator = new QueryExpressionEvaluatorImpl();

    private boolean eval(String query, String rawJson) {
        QueryExpression expression = queryParser.parse(queryLexer.tokenize(query));
        return evaluator.evaluate(json(rawJson), expression).asBoolean();
    }

    private JsonNode json(String raw) {
        return new JsonParser(new JsonLexer(raw).tokenize()).parse();
    }

    // EXISTS

    @Test
    void returnsTrueWhenFieldExists() {
        assertTrue(eval(".name EXISTS", "{\"name\": \"Jan\"}"));
    }

    @Test
    void returnsFalseWhenFieldDoesNotExist() {
        assertFalse(eval(".name EXISTS", "{\"age\": 30}"));
    }

    @Test
    void returnsTrueWhenNestedFieldExists() {
        assertTrue(eval(".address.city EXISTS", "{\"address\": {\"city\": \"Warsaw\"}}"));
    }

    @Test
    void returnsFalseWhenIntermediateSegmentDoesNotExist() {
        assertFalse(eval(".address.city EXISTS", "{\"age\": 30}"));
    }

    @Test
    void returnsFalseWhenIntermediateSegmentIsNotAnObject() {
        assertFalse(eval(".address.city EXISTS", "{\"address\": \"Warsaw\"}"));
    }

    // NOT

    @Test
    void notNegatesExists() {
        assertTrue(eval("NOT .deleted EXISTS", "{\"name\": \"Jan\"}"));
    }

    @Test
    void notNegatesExistsWhenFieldPresent() {
        assertFalse(eval("NOT .name EXISTS", "{\"name\": \"Jan\"}"));
    }

    // comparison - string

    @Test
    void stringEqualityMatches() {
        assertTrue(eval(".name == \"Warsaw\"", "{\"name\": \"Warsaw\"}"));
    }

    @Test
    void stringEqualityDoesNotMatchDifferentValue() {
        assertFalse(eval(".name == \"Warsaw\"", "{\"name\": \"Krakow\"}"));
    }

    @Test
    void stringInequalityMatches() {
        assertTrue(eval(".name != \"Krakow\"", "{\"name\": \"Warsaw\"}"));
    }

    @Test
    void stringComparisonWithUnsupportedOperatorThrows() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval(".name > \"Adam\"", "{\"name\": \"Jan\"}")
        );
    }

    @Test
    void stringComparisonThrowsWhenFieldMissing() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval(".name == \"Warsaw\"", "{\"age\": 30}")
        );
    }

    @Test
    void stringComparisonReturnsFalseWhenTypeDoesNotMatch() {
        assertFalse(eval(".age == \"30\"", "{\"age\": 30}"));
    }

    // comparison - number

    @Test
    void numberEqualityMatches() {
        assertTrue(eval(".age == 30", "{\"age\": 30}"));
    }

    @Test
    void numberGreaterThanMatches() {
        assertTrue(eval(".age > 18", "{\"age\": 30}"));
    }

    @Test
    void numberGreaterThanDoesNotMatchWhenEqual() {
        assertFalse(eval(".age > 30", "{\"age\": 30}"));
    }

    @Test
    void numberGreaterThanOrEqualMatchesWhenEqual() {
        assertTrue(eval(".age >= 30", "{\"age\": 30}"));
    }

    @Test
    void numberLessThanMatches() {
        assertTrue(eval(".age < 18", "{\"age\": 10}"));
    }

    @Test
    void numberLessThanOrEqualMatchesWhenEqual() {
        assertTrue(eval(".age <= 30", "{\"age\": 30}"));
    }

    @Test
    void numberInequalityMatches() {
        assertTrue(eval(".age != 18", "{\"age\": 30}"));
    }

    @Test
    void numberComparisonThrowsWhenFieldMissing() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval(".age > 18", "{\"name\": \"Jan\"}")
        );
    }

    // comparison - boolean

    @Test
    void booleanEqualityMatchesTrue() {
        assertTrue(eval(".active == true", "{\"active\": true}"));
    }

    @Test
    void booleanEqualityDoesNotMatchFalse() {
        assertFalse(eval(".active == true", "{\"active\": false}"));
    }

    @Test
    void booleanInequalityMatches() {
        assertTrue(eval(".active != true", "{\"active\": false}"));
    }

    @Test
    void booleanComparisonWithUnsupportedOperatorThrows() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval(".active > true", "{\"active\": true}")
        );
    }

    // comparison - null

    @Test
    void nullEqualityMatchesNullField() {
        assertTrue(eval(".deleted == null", "{\"deleted\": null}"));
    }

    @Test
    void nullEqualityDoesNotMatchNonNullField() {
        assertFalse(eval(".deleted == null", "{\"deleted\": \"2024\"}"));
    }

    @Test
    void nullInequalityMatchesNonNullField() {
        assertTrue(eval(".deleted != null", "{\"deleted\": \"2024\"}"));
    }

    @Test
    void nullComparisonWithUnsupportedOperatorThrows() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval(".deleted > null", "{\"deleted\": null}")
        );
    }

    // logical

    @Test
    void andReturnsTrueWhenBothMatch() {
        assertTrue(eval(".age > 18 AND .active == true", "{\"age\": 30, \"active\": true}"));
    }

    @Test
    void andReturnsFalseWhenOneDoesNotMatch() {
        assertFalse(eval(".age > 18 AND .active == true", "{\"age\": 30, \"active\": false}"));
    }

    @Test
    void orReturnsTrueWhenOneMatches() {
        assertTrue(eval(".role == \"admin\" OR .role == \"moderator\"", "{\"role\": \"moderator\"}"));
    }

    @Test
    void orReturnsFalseWhenNoneMatch() {
        assertFalse(eval(".role == \"admin\" OR .role == \"moderator\"", "{\"role\": \"user\"}"));
    }

    @Test
    void complexQueryMatches() {
        assertTrue(eval(".age >= 18 AND .active == true AND NOT .deleted EXISTS", "{\"age\": 25, \"active\": true}"));
    }

    @Test
    void complexQueryDoesNotMatchWhenDeletedPresent() {
        assertFalse(eval(".age >= 18 AND .active == true AND NOT .deleted EXISTS", "{\"age\": 25, \"active\": true, \"deleted\": null}"));
    }

    // matches

    @Test
    void matchesReturnsTrueForFullRegexMatch() {
        assertTrue(eval("matches(.code, \"[0-9]+\")", "{\"code\": \"12345\"}"));
    }

    @Test
    void matchesReturnsFalseWhenOnlyFragmentMatches() {
        assertFalse(eval("matches(.code, \"[0-9]+\")", "{\"code\": \"A123\"}"));
    }

    @Test
    void matchesThrowsForInvalidRegex() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval("matches(.code, \"[\")", "{\"code\": \"123\"}")
        );
    }

    @Test
    void matchesThrowsForWrongValueType() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval("matches(.code, \"[0-9]+\")", "{\"code\": 123}")
        );
    }

    @Test
    void matchesThrowsForMissingPath() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval("matches(.code, \"[0-9]+\")", "{\"name\": \"Jan\"}")
        );
    }

    // size

    @Test
    void sizeWorksForArray() {
        assertTrue(eval("size(.tags) == 2", "{\"tags\": [\"user\", \"admin\"]}"));
    }

    @Test
    void sizeWorksForString() {
        assertTrue(eval("size(.name) > 3", "{\"name\": \"John\"}"));
    }

    @Test
    void sizeWorksForObject() {
        assertTrue(eval("size(.profile) >= 2", "{\"profile\": {\"name\": \"Jan\", \"age\": 30}}"));
    }

    @Test
    void sizeSupportsAllNumberComparisonOperators() {
        assertTrue(eval("size(.tags) != 1", "{\"tags\": [1, 2]}"));
        assertTrue(eval("size(.tags) > 1", "{\"tags\": [1, 2]}"));
        assertTrue(eval("size(.tags) >= 2", "{\"tags\": [1, 2]}"));
        assertTrue(eval("size(.tags) < 3", "{\"tags\": [1, 2]}"));
        assertTrue(eval("size(.tags) <= 2", "{\"tags\": [1, 2]}"));
    }

    @Test
    void sizeThrowsForUnsupportedType() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval("size(.age) > 0", "{\"age\": 30}")
        );
    }

    @Test
    void sizeThrowsForMissingPath() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval("size(.tags) > 0", "{\"name\": \"Jan\"}")
        );
    }

    @Test
    void whereRequiresBooleanFinalValue() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval("size(.tags)", "{\"tags\": [1, 2]}")
        );
    }

    @Test
    void isExpressionRemainsUnsupported() {
        assertThrows(QueryExpressionEvaluatorException.class, () ->
            eval(".age IS NUMBER", "{\"age\": 30}")
        );
    }
}
