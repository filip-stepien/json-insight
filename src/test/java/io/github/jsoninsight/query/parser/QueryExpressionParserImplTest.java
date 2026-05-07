package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.expression.node.*;
import io.github.jsoninsight.query.ast.expression.operator.JsonType;
import io.github.jsoninsight.query.ast.expression.operator.ComparisonOperator;
import io.github.jsoninsight.query.ast.expression.operator.LogicalOperator;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.impl.QueryExpressionParserImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryExpressionParserImplTest {

    private static final QueryLexerImpl lexer = new QueryLexerImpl();
    private static final QueryExpressionParserImpl parser = new QueryExpressionParserImpl();

    private QueryExpression parse(String input) {
        return parser.parse(lexer.tokenize(input));
    }

    // comparisons

    @Test
    void parsesEqualityComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".age == 25"));
        JsonPathNode left = assertInstanceOf(JsonPathNode.class, node.leftExpression());
        NumberLiteralNode right = assertInstanceOf(NumberLiteralNode.class, node.rightExpression());
        assertEquals(".age", left.pathValue());
        assertEquals(ComparisonOperator.EQ, node.operator());
        assertEquals(new BigDecimal("25"), right.value());
    }

    @Test
    void parsesNotEqualComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".status != \"inactive\""));
        assertEquals(ComparisonOperator.NEQ, node.operator());
        StringLiteralNode right = assertInstanceOf(StringLiteralNode.class, node.rightExpression());
        assertEquals("inactive", right.value());
    }

    @Test
    void parsesGreaterThanComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".age > 18"));
        assertEquals(ComparisonOperator.GT, node.operator());
    }

    @Test
    void parsesGreaterThanOrEqualComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".age >= 18"));
        assertEquals(ComparisonOperator.GTE, node.operator());
    }

    @Test
    void parsesLessThanComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".age < 18"));
        assertEquals(ComparisonOperator.LT, node.operator());
    }

    @Test
    void parsesLessThanOrEqualComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".age <= 18"));
        assertEquals(ComparisonOperator.LTE, node.operator());
    }

    // literals

    @Test
    void parsesStringLiteral() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".name == \"John\""));
        StringLiteralNode right = assertInstanceOf(StringLiteralNode.class, node.rightExpression());
        assertEquals("John", right.value());
    }

    @Test
    void parsesNegativeNumber() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".temperature == -5"));
        NumberLiteralNode right = assertInstanceOf(NumberLiteralNode.class, node.rightExpression());
        assertEquals(new BigDecimal("-5"), right.value());
    }

    @Test
    void parsesBooleanLiteral() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".active == true"));
        BooleanLiteralNode right = assertInstanceOf(BooleanLiteralNode.class, node.rightExpression());
        assertTrue(right.value());
    }

    @Test
    void parsesNullLiteral() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".deleted == null"));
        assertInstanceOf(NullLiteralNode.class, node.rightExpression());
    }

    // exists / is

    @Test
    void parsesExistsExpression() {
        ExistsNode node = assertInstanceOf(ExistsNode.class, parse(".name EXISTS"));
        assertEquals(".name", node.path().pathValue());
    }

    @Test
    void parsesIsExpression() {
        IsNode node = assertInstanceOf(IsNode.class, parse(".age IS NUMBER"));
        assertEquals(".age", node.path().pathValue());
        assertEquals(JsonType.NUMBER, node.dataType());
    }

    // logical

    @Test
    void parsesAndExpression() {
        LogicalNode node = assertInstanceOf(LogicalNode.class, parse(".age > 18 AND .active == true"));
        assertEquals(LogicalOperator.AND, node.operator());
        assertInstanceOf(ComparisonNode.class, node.leftExpression());
        assertInstanceOf(ComparisonNode.class, node.rightExpression());
    }

    @Test
    void parsesOrExpression() {
        LogicalNode node = assertInstanceOf(LogicalNode.class, parse(".age < 5 OR .age > 18"));
        assertEquals(LogicalOperator.OR, node.operator());
    }

    @Test
    void parsesNotExpression() {
        NotNode node = assertInstanceOf(NotNode.class, parse("NOT .active == true"));
        assertInstanceOf(ComparisonNode.class, node.operand());
    }

    @Test
    void parsesDoubleNot() {
        NotNode node = assertInstanceOf(NotNode.class, parse("NOT NOT .active == true"));
        assertInstanceOf(NotNode.class, node.operand());
    }

    @Test
    void andBindsTighterThanOr() {
        LogicalNode node = assertInstanceOf(LogicalNode.class, parse(".a == 1 OR .b == 2 AND .c == 3"));
        assertEquals(LogicalOperator.OR, node.operator());
        assertInstanceOf(ComparisonNode.class, node.leftExpression());
        LogicalNode right = assertInstanceOf(LogicalNode.class, node.rightExpression());
        assertEquals(LogicalOperator.AND, right.operator());
    }

    // parentheses

    @Test
    void parsesParenthesizedExpression() {
        LogicalNode node = assertInstanceOf(LogicalNode.class, parse("(.age > 18 AND .active == true) OR .admin == true"));
        assertEquals(LogicalOperator.OR, node.operator());
        assertInstanceOf(LogicalNode.class, node.leftExpression());
    }

    // functions

    @Test
    void parsesMatchesFunction() {
        MatchesNode node = assertInstanceOf(MatchesNode.class, parse("matches(.code, \"[0-9]+\")"));
        JsonPathNode value = assertInstanceOf(JsonPathNode.class, node.value());
        StringLiteralNode pattern = assertInstanceOf(StringLiteralNode.class, node.pattern());
        assertEquals(".code", value.pathValue());
        assertEquals("[0-9]+", pattern.value());
    }

    @Test
    void parsesSizeFunctionAsStandaloneExpression() {
        SizeNode node = assertInstanceOf(SizeNode.class, parse("size(.tags)"));
        JsonPathNode value = assertInstanceOf(JsonPathNode.class, node.value());
        assertEquals(".tags", value.pathValue());
    }

    @Test
    void parsesSizeFunctionInComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse("size(.tags) > 1"));
        SizeNode left = assertInstanceOf(SizeNode.class, node.leftExpression());
        NumberLiteralNode right = assertInstanceOf(NumberLiteralNode.class, node.rightExpression());
        assertInstanceOf(JsonPathNode.class, left.value());
        assertEquals(ComparisonOperator.GT, node.operator());
        assertEquals(new BigDecimal("1"), right.value());
    }

    @Test
    void parsesCombinedMatchesAndSizeExpression() {
        LogicalNode node = assertInstanceOf(
            LogicalNode.class,
            parse("matches(.code, \"[0-9]+\") AND size(.tags) > 1")
        );
        assertInstanceOf(MatchesNode.class, node.leftExpression());
        assertInstanceOf(ComparisonNode.class, node.rightExpression());
    }

    @Test
    void functionNamesAreCaseInsensitive() {
        assertInstanceOf(MatchesNode.class, parse("MATCHES(.code, \"[0-9]+\")"));
        assertInstanceOf(SizeNode.class, parse("SIZE(.tags)"));
    }

    // nested pathValue

    @Test
    void parsesNestedPath() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".address.city == \"Warsaw\""));
        JsonPathNode left = assertInstanceOf(JsonPathNode.class, node.leftExpression());
        assertEquals(".address.city", left.pathValue());
    }

    // exceptions

    @Test
    void throwsForNullTokenList() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    void throwsForEmptyTokenList() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of()));
    }

    @Test
    void throwsForUnknownFunction() {
        assertThrows(QueryExpressionParserException.class, () -> parse("contains(.tags, \"admin\")"));
    }

    @Test
    void throwsForMissingClosingParen() {
        assertThrows(QueryExpressionParserException.class, () -> parse("(.age == 25"));
    }

    @Test
    void throwsForUnexpectedToken() {
        assertThrows(QueryExpressionParserException.class, () -> parse("== 25"));
    }
}
