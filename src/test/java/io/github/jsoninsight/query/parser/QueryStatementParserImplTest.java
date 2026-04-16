package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.ast.predicate.node.JsonPathNode;
import io.github.jsoninsight.query.ast.statement.QueryStatement;
import io.github.jsoninsight.query.ast.statement.clause.FromClause;
import io.github.jsoninsight.query.ast.statement.clause.SelectClause;
import io.github.jsoninsight.query.ast.statement.clause.WhereClause;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.QueryParserException;
import io.github.jsoninsight.query.parser.QueryStatementParserException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryStatementParserImplTest {

    private static final QueryLexerImpl lexer = new QueryLexerImpl();
    private static final QueryStatementParser parser = QueryParserFactory.createStatementParser();

    private QueryStatement parse(String input) {
        return parser.parse(lexer.tokenize(input));
    }

    // SELECT *

    @Test
    void parsesSelectWildcard() {
        QueryStatement statement = parse("SELECT * FROM users");
        assertInstanceOf(SelectClause.Wildcard.class, statement.select());
    }

    // SELECT fields

    @Test
    void parsesSelectSingleField() {
        QueryStatement statement = parse("SELECT .name FROM users");
        SelectClause.Fields fields = assertInstanceOf(SelectClause.Fields.class, statement.select());
        assertEquals(List.of(new JsonPathNode(".name")), fields.paths());
    }

    @Test
    void parsesSelectNestedPath() {
        QueryStatement statement = parse("SELECT .address.city FROM users");
        SelectClause.Fields fields = assertInstanceOf(SelectClause.Fields.class, statement.select());
        assertEquals(List.of(new JsonPathNode(".address.city")), fields.paths());
    }

    @Test
    void parsesSelectMultipleFields() {
        QueryStatement statement = parse("SELECT .name, .age FROM users");
        SelectClause.Fields fields = assertInstanceOf(SelectClause.Fields.class, statement.select());
        assertEquals(
            List.of(new JsonPathNode(".name"), new JsonPathNode(".age")),
            fields.paths()
        );
    }

    // FROM

    @Test
    void parsesFromClause() {
        QueryStatement statement = parse("SELECT * FROM users");
        assertEquals(new FromClause("users"), statement.from());
    }

    // WHERE

    @Test
    void parsesStatementWithoutWhere() {
        QueryStatement statement = parse("SELECT * FROM users");
        assertTrue(statement.where().isEmpty());
    }

    @Test
    void parsesWhereClausePredicate() {
        QueryStatement statement = parse("SELECT * FROM users WHERE .age >= 18");
        WhereClause where = statement.where().get();
        assertNotNull(where.predicate());
    }

    // combined

    @Test
    void parsesFullStatement() {
        QueryStatement statement = parse("SELECT .name, .age FROM users WHERE .active == true AND .age >= 18");
        assertInstanceOf(SelectClause.Fields.class, statement.select());
        assertEquals(new FromClause("users"), statement.from());
        assertTrue(statement.where().isPresent());
    }

    // errors

    @Test
    void throwsWhenSelectMissing() {
        assertThrows(QueryStatementParserException.class, () -> parse("* FROM users"));
    }

    @Test
    void throwsWhenFromMissing() {
        assertThrows(QueryStatementParserException.class, () -> parse("SELECT * users"));
    }

    @Test
    void throwsWhenCollectionNameMissing() {
        assertThrows(QueryStatementParserException.class, () -> parse("SELECT * FROM"));
    }

    @Test
    void throwsWhenCommaMissingBetweenFields() {
        assertThrows(QueryStatementParserException.class, () -> parse("SELECT .name .age FROM users"));
    }

    @Test
    void throwsWhenTrailingCommaInSelect() {
        assertThrows(QueryStatementParserException.class, () -> parse("SELECT .name, FROM users"));
    }

    @Test
    void throwsWhenNoFieldsInSelect() {
        assertThrows(QueryStatementParserException.class, () -> parse("SELECT FROM users"));
    }

    @Test
    void throwsWhenSelectStartsWithComma() {
        assertThrows(QueryStatementParserException.class, () -> parse("SELECT , FROM users"));
    }

    @Test
    void throwsWhenWhereHasNoPredicate() {
        assertThrows(QueryParserException.class, () -> parse("SELECT * FROM users WHERE"));
    }
}
