package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.json.JsonLexer;
import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.json.JsonParser;
import io.github.jsoninsight.query.ast.statement.QueryStatement;
import io.github.jsoninsight.query.evaluator.impl.QueryPredicateEvaluatorImpl;
import io.github.jsoninsight.query.evaluator.impl.QueryStatementEvaluatorImpl;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.QueryParserFactory;
import io.github.jsoninsight.query.parser.QueryStatementParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryStatementEvaluatorImplTest {

    private static final QueryStatementEvaluator evaluator = new QueryStatementEvaluatorImpl(new QueryPredicateEvaluatorImpl());
    private static final QueryStatementParser parser = QueryParserFactory.createStatementParser();
    private static final QueryLexerImpl lexer = new QueryLexerImpl();

    private QueryStatement parse(String query) {
        return parser.parse(lexer.tokenize(query));
    }

    private List<JsonNode> eval(String query, Map<String, List<JsonNode>> collections) {
        return evaluator.evaluate(parse(query), collections);
    }

    private static JsonNode doc(String json) {
        return new JsonParser(new JsonLexer(json).tokenize()).parse();
    }

    private static Map<String, List<JsonNode>> collections(String name, JsonNode... docs) {
        return Map.of(name, List.of(docs));
    }

    // SELECT *

    @Test
    void selectWildcardReturnsAllDocuments() {
        var collections = collections("users",
            doc("{\"name\": \"Alice\"}"),
            doc("{\"name\": \"Bob\"}")
        );
        assertEquals(2, eval("SELECT * FROM users", collections).size());
    }

    @Test
    void selectWildcardReturnsFullDocuments() {
        var collections = collections("users", doc("{\"name\": \"Alice\", \"age\": 30}"));
        JsonNode result = eval("SELECT * FROM users", collections).getFirst();
        assertInstanceOf(JsonNode.ObjectNode.class, result);
        JsonNode.ObjectNode obj = (JsonNode.ObjectNode) result;
        assertEquals(2, obj.fields().size());
    }

    // WHERE

    @Test
    void whereClauseFiltersDocuments() {
        var collections = collections("users",
            doc("{\"age\": 17}"),
            doc("{\"age\": 18}"),
            doc("{\"age\": 25}")
        );
        List<JsonNode> result = eval("SELECT * FROM users WHERE .age >= 18", collections);
        assertEquals(2, result.size());
    }

    @Test
    void whereClauseWithNoMatchReturnsEmptyList() {
        var collections = collections("users", doc("{\"age\": 10}"));
        List<JsonNode> result = eval("SELECT * FROM users WHERE .age >= 18", collections);
        assertTrue(result.isEmpty());
    }

    // SELECT fields

    @Test
    void selectFieldsProjectsDocuments() {
        var collections = collections("users",
            doc("{\"name\": \"Alice\", \"age\": 30, \"active\": true}")
        );
        List<JsonNode> result = eval("SELECT .name FROM users", collections);
        JsonNode.ObjectNode obj = assertInstanceOf(JsonNode.ObjectNode.class, result.getFirst());
        assertEquals(1, obj.fields().size());
        assertInstanceOf(JsonNode.StringNode.class, obj.fields().get("name"));
    }

    @Test
    void selectMultipleFieldsProjectsDocuments() {
        var collections = collections("users",
            doc("{\"name\": \"Alice\", \"age\": 30, \"active\": true}")
        );
        List<JsonNode> result = eval("SELECT .name, .age FROM users", collections);
        JsonNode.ObjectNode obj = assertInstanceOf(JsonNode.ObjectNode.class, result.getFirst());
        assertEquals(2, obj.fields().size());
        assertTrue(obj.fields().containsKey("name"));
        assertTrue(obj.fields().containsKey("age"));
    }

    @Test
    void selectNestedPathUsesLastSegmentAsKey() {
        var collections = collections("users",
            doc("{\"address\": {\"city\": \"Kraków\"}}")
        );
        List<JsonNode> result = eval("SELECT .address.city FROM users", collections);
        JsonNode.ObjectNode obj = assertInstanceOf(JsonNode.ObjectNode.class, result.getFirst());
        assertTrue(obj.fields().containsKey("city"));
        assertEquals(new JsonNode.StringNode("Kraków"), obj.fields().get("city"));
    }

    @Test
    void selectMissingFieldIsOmittedFromResult() {
        var collections = collections("users",
            doc("{\"name\": \"Alice\"}"),
            doc("{\"name\": \"Bob\", \"age\": 25}")
        );
        List<JsonNode> result = eval("SELECT .age FROM users", collections);
        JsonNode.ObjectNode alice = assertInstanceOf(JsonNode.ObjectNode.class, result.get(0));
        JsonNode.ObjectNode bob = assertInstanceOf(JsonNode.ObjectNode.class, result.get(1));
        assertTrue(alice.fields().isEmpty());
        assertEquals(1, bob.fields().size());
    }

    // combined

    @Test
    void fullQueryFiltersAndProjects() {
        var collections = collections("users",
            doc("{\"name\": \"Alice\", \"age\": 17}"),
            doc("{\"name\": \"Bob\", \"age\": 25}")
        );
        List<JsonNode> result = eval("SELECT .name FROM users WHERE .age >= 18", collections);
        assertEquals(1, result.size());
        JsonNode.ObjectNode obj = assertInstanceOf(JsonNode.ObjectNode.class, result.getFirst());
        assertEquals(new JsonNode.StringNode("Bob"), obj.fields().get("name"));
    }

    // edge cases

    @Test
    void nonObjectDocumentIsFilteredOutByWhereClause() {
        var collections = collections("data",
            doc("[1, 2, 3]"),
            doc("{\"age\": 25}")
        );
        List<JsonNode> result = eval("SELECT * FROM data WHERE .age >= 18", collections);
        assertEquals(1, result.size());
    }

    @Test
    void nonObjectDocumentInSelectFieldsYieldsEmptyObject() {
        var collections = collections("data", doc("[1, 2, 3]"));
        List<JsonNode> result = eval("SELECT .age FROM data", collections);
        JsonNode.ObjectNode obj = assertInstanceOf(JsonNode.ObjectNode.class, result.getFirst());
        assertTrue(obj.fields().isEmpty());
    }

    @Test
    void emptyCollectionReturnsEmptyList() {
        var collections = Map.of("users", List.<JsonNode>of());
        assertTrue(eval("SELECT * FROM users", collections).isEmpty());
    }

    @Test
    void unknownCollectionThrows() {
        var collections = collections("users", doc("{\"name\": \"Alice\"}"));
        assertThrows(QueryStatementEvaluatorException.class, () -> eval("SELECT * FROM orders", collections));
    }
}
