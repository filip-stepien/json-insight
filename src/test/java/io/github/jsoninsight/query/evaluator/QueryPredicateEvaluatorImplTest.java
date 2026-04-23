package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.json.JsonParser;
import io.github.jsoninsight.json.JsonLexer;
import io.github.jsoninsight.query.ast.predicate.QueryPredicateExpression;
import io.github.jsoninsight.query.evaluator.impl.QueryPredicateEvaluatorImpl;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.impl.QueryPredicateParserImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryPredicateEvaluatorImplTest {

    private static final QueryLexerImpl queryLexer = new QueryLexerImpl();
    private static final QueryPredicateParserImpl queryParser = new QueryPredicateParserImpl();
    private static final QueryPredicateEvaluator evaluator = new QueryPredicateEvaluatorImpl();

    private boolean eval(String query, String rawJson) {
        QueryPredicateExpression predicate = queryParser.parse(queryLexer.tokenize(query));
        return evaluator.evaluate(json(rawJson), predicate);
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
        assertThrows(QueryPredicateEvaluatorException.class, () ->
            eval(".name > \"Adam\"", "{\"name\": \"Jan\"}")
        );
    }

    @Test
    void stringComparisonReturnsFalseWhenFieldMissing() {
        assertFalse(eval(".name == \"Warsaw\"", "{\"age\": 30}"));
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
    void numberComparisonReturnsFalseWhenFieldMissing() {
        assertFalse(eval(".age > 18", "{\"name\": \"Jan\"}"));
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
        assertThrows(QueryPredicateEvaluatorException.class, () ->
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
        assertThrows(QueryPredicateEvaluatorException.class, () ->
            eval(".deleted > null", "{\"deleted\": null}")
        );
    }

    // logical - AND

    @Test
    void andReturnsTrueWhenBothMatch() {
        assertTrue(eval(".age > 18 AND .active == true", "{\"age\": 30, \"active\": true}"));
    }

    @Test
    void andReturnsFalseWhenOneDoesNotMatch() {
        assertFalse(eval(".age > 18 AND .active == true", "{\"age\": 30, \"active\": false}"));
    }

    // logical - OR

    @Test
    void orReturnsTrueWhenOneMatches() {
        assertTrue(eval(".role == \"admin\" OR .role == \"moderator\"", "{\"role\": \"moderator\"}"));
    }

    @Test
    void orReturnsFalseWhenNoneMatch() {
        assertFalse(eval(".role == \"admin\" OR .role == \"moderator\"", "{\"role\": \"user\"}"));
    }

    // combined

    @Test
    void complexQueryMatches() {
        assertTrue(eval(".age >= 18 AND .active == true AND NOT .deleted EXISTS", "{\"age\": 25, \"active\": true}"));
    }

    @Test
    void complexQueryDoesNotMatchWhenDeletedPresent() {
        assertFalse(eval(".age >= 18 AND .active == true AND NOT .deleted EXISTS", "{\"age\": 25, \"active\": true, \"deleted\": null}"));
    }
}
