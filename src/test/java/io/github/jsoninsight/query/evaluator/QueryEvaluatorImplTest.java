package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.json.JsonParser;
import io.github.jsoninsight.json.JsonLexer;
import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.evaluator.impl.QueryEvaluatorImpl;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.impl.QueryParserImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryEvaluatorImplTest {

    private static final QueryLexerImpl queryLexer = new QueryLexerImpl();
    private static final QueryParserImpl queryParser = new QueryParserImpl();

    private QueryEvaluator evaluatorFor(String query) {
        QueryExpressionNode ast = queryParser.parse(queryLexer.tokenize(query));
        return new QueryEvaluatorImpl(ast);
    }

    private JsonNode json(String raw) {
        return new JsonParser(new JsonLexer(raw).tokenize()).parse();
    }

    // EXISTS

    @Test
    void returnsTrueWhenFieldExists() {
        assertTrue(evaluatorFor(".name EXISTS").evaluate(json("{\"name\": \"Jan\"}")));
    }

    @Test
    void returnsFalseWhenFieldDoesNotExist() {
        assertFalse(evaluatorFor(".name EXISTS").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void returnsTrueWhenNestedFieldExists() {
        assertTrue(evaluatorFor(".address.city EXISTS").evaluate(json("{\"address\": {\"city\": \"Warsaw\"}}")));
    }

    @Test
    void returnsFalseWhenIntermediateSegmentDoesNotExist() {
        assertFalse(evaluatorFor(".address.city EXISTS").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void returnsFalseWhenIntermediateSegmentIsNotAnObject() {
        assertFalse(evaluatorFor(".address.city EXISTS").evaluate(json("{\"address\": \"Warsaw\"}")));
    }

    // NOT

    @Test
    void notNegatesExists() {
        assertTrue(evaluatorFor("NOT .deleted EXISTS").evaluate(json("{\"name\": \"Jan\"}")));
    }

    @Test
    void notNegatesExistsWhenFieldPresent() {
        assertFalse(evaluatorFor("NOT .name EXISTS").evaluate(json("{\"name\": \"Jan\"}")));
    }

    // comparison - string

    @Test
    void stringEqualityMatches() {
        assertTrue(evaluatorFor(".name == \"Warsaw\"").evaluate(json("{\"name\": \"Warsaw\"}")));
    }

    @Test
    void stringEqualityDoesNotMatchDifferentValue() {
        assertFalse(evaluatorFor(".name == \"Warsaw\"").evaluate(json("{\"name\": \"Krakow\"}")));
    }

    @Test
    void stringInequalityMatches() {
        assertTrue(evaluatorFor(".name != \"Krakow\"").evaluate(json("{\"name\": \"Warsaw\"}")));
    }

    @Test
    void stringComparisonWithUnsupportedOperatorThrows() {
        assertThrows(QueryEvaluatorException.class, () ->
            evaluatorFor(".name > \"Adam\"").evaluate(json("{\"name\": \"Jan\"}"))
        );
    }

    @Test
    void stringComparisonReturnsFalseWhenFieldMissing() {
        assertFalse(evaluatorFor(".name == \"Warsaw\"").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void stringComparisonReturnsFalseWhenTypeDoesNotMatch() {
        assertFalse(evaluatorFor(".age == \"30\"").evaluate(json("{\"age\": 30}")));
    }

    // comparison - number

    @Test
    void numberEqualityMatches() {
        assertTrue(evaluatorFor(".age == 30").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void numberGreaterThanMatches() {
        assertTrue(evaluatorFor(".age > 18").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void numberGreaterThanDoesNotMatchWhenEqual() {
        assertFalse(evaluatorFor(".age > 30").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void numberGreaterThanOrEqualMatchesWhenEqual() {
        assertTrue(evaluatorFor(".age >= 30").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void numberLessThanMatches() {
        assertTrue(evaluatorFor(".age < 18").evaluate(json("{\"age\": 10}")));
    }

    @Test
    void numberLessThanOrEqualMatchesWhenEqual() {
        assertTrue(evaluatorFor(".age <= 30").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void numberInequalityMatches() {
        assertTrue(evaluatorFor(".age != 18").evaluate(json("{\"age\": 30}")));
    }

    @Test
    void numberComparisonReturnsFalseWhenFieldMissing() {
        assertFalse(evaluatorFor(".age > 18").evaluate(json("{\"name\": \"Jan\"}")));
    }

    // comparison - boolean

    @Test
    void booleanEqualityMatchesTrue() {
        assertTrue(evaluatorFor(".active == true").evaluate(json("{\"active\": true}")));
    }

    @Test
    void booleanEqualityDoesNotMatchFalse() {
        assertFalse(evaluatorFor(".active == true").evaluate(json("{\"active\": false}")));
    }

    @Test
    void booleanInequalityMatches() {
        assertTrue(evaluatorFor(".active != true").evaluate(json("{\"active\": false}")));
    }

    @Test
    void booleanComparisonWithUnsupportedOperatorThrows() {
        assertThrows(QueryEvaluatorException.class, () ->
            evaluatorFor(".active > true").evaluate(json("{\"active\": true}"))
        );
    }

    // comparison - null

    @Test
    void nullEqualityMatchesNullField() {
        assertTrue(evaluatorFor(".deleted == null").evaluate(json("{\"deleted\": null}")));
    }

    @Test
    void nullEqualityDoesNotMatchNonNullField() {
        assertFalse(evaluatorFor(".deleted == null").evaluate(json("{\"deleted\": \"2024\"}")));
    }

    @Test
    void nullInequalityMatchesNonNullField() {
        assertTrue(evaluatorFor(".deleted != null").evaluate(json("{\"deleted\": \"2024\"}")));
    }

    @Test
    void nullComparisonWithUnsupportedOperatorThrows() {
        assertThrows(QueryEvaluatorException.class, () ->
            evaluatorFor(".deleted > null").evaluate(json("{\"deleted\": null}"))
        );
    }

    // logical - AND

    @Test
    void andReturnsTrueWhenBothMatch() {
        assertTrue(evaluatorFor(".age > 18 AND .active == true").evaluate(
            json("{\"age\": 30, \"active\": true}")
        ));
    }

    @Test
    void andReturnsFalseWhenOneDoesNotMatch() {
        assertFalse(evaluatorFor(".age > 18 AND .active == true").evaluate(
            json("{\"age\": 30, \"active\": false}")
        ));
    }

    // logical - OR

    @Test
    void orReturnsTrueWhenOneMatches() {
        assertTrue(evaluatorFor(".role == \"admin\" OR .role == \"moderator\"").evaluate(
            json("{\"role\": \"moderator\"}")
        ));
    }

    @Test
    void orReturnsFalseWhenNoneMatch() {
        assertFalse(evaluatorFor(".role == \"admin\" OR .role == \"moderator\"").evaluate(
            json("{\"role\": \"user\"}")
        ));
    }

    // combined

    @Test
    void complexQueryMatches() {
        assertTrue(evaluatorFor(".age >= 18 AND .active == true AND NOT .deleted EXISTS").evaluate(
            json("{\"age\": 25, \"active\": true}")
        ));
    }

    @Test
    void complexQueryDoesNotMatchWhenDeletedPresent() {
        assertFalse(evaluatorFor(".age >= 18 AND .active == true AND NOT .deleted EXISTS").evaluate(
            json("{\"age\": 25, \"active\": true, \"deleted\": null}")
        ));
    }
}
