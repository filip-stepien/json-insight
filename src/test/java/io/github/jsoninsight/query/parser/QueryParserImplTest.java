package io.github.jsoninsight.query.parser;

import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.node.*;
import io.github.jsoninsight.query.ast.operator.JsonType;
import io.github.jsoninsight.query.ast.operator.ComparisonOperator;
import io.github.jsoninsight.query.ast.operator.LogicalOperator;
import io.github.jsoninsight.query.lexer.impl.QueryLexerImpl;
import io.github.jsoninsight.query.parser.impl.QueryParserImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserImplTest {

    private static final QueryLexerImpl lexer = new QueryLexerImpl();
    private static final QueryParserImpl parser = new QueryParserImpl();

    private QueryExpressionNode parse(String input) {
        return parser.parse(lexer.tokenize(input));
    }

    // comparisons

    @Test
    void parsesEqualityComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".age == 25"));
        assertEquals(".age", node.path().pathValue());
        assertEquals(ComparisonOperator.EQ, node.operator());
        NumberLiteralNode right = (NumberLiteralNode) node.rightValue();
        assertEquals(new BigDecimal("25"), right.value());
    }

    @Test
    void parsesNotEqualComparison() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".status != \"inactive\""));
        assertEquals(ComparisonOperator.NEQ, node.operator());
        StringLiteralNode right = (StringLiteralNode) node.rightValue();
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
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".functionName == \"John\""));
        StringLiteralNode right = (StringLiteralNode) node.rightValue();
        assertEquals("John", right.value());
    }

    @Test
    void parsesNegativeNumber() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".temperature == -5"));
        NumberLiteralNode right = (NumberLiteralNode) node.rightValue();
        assertEquals(new BigDecimal("-5"), right.value());
    }

    @Test
    void parsesBooleanLiteral() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".active == true"));
        BooleanLiteralNode right = (BooleanLiteralNode) node.rightValue();
        assertTrue(right.value());
    }

    @Test
    void parsesNullLiteral() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".rightValue == null"));
        assertInstanceOf(NullLiteralNode.class, node.rightValue());
    }

    // exists / is

    @Test
    void parsesExistsExpression() {
        ExistsNode node = assertInstanceOf(ExistsNode.class, parse(".functionName EXISTS"));
        assertEquals(".functionName", node.path().pathValue());
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
    void parsesFunctionCallWithPathAndStringArg() {
        FunctionCallNode node = assertInstanceOf(FunctionCallNode.class, parse("contains(.tags, \"admin\")"));
        assertEquals("contains", node.functionName());
        assertEquals(2, node.args().size());
        assertInstanceOf(JsonPathNode.class, node.args().getFirst());
        assertInstanceOf(StringLiteralNode.class, node.args().getLast());
    }

    @Test
    void parsesFunctionCallWithNoArgs() {
        FunctionCallNode node = assertInstanceOf(FunctionCallNode.class, parse("isEmpty()"));
        assertEquals("isEmpty", node.functionName());
        assertEquals(0, node.args().size());
    }

    // nested pathValue

    @Test
    void parsesNestedPath() {
        ComparisonNode node = assertInstanceOf(ComparisonNode.class, parse(".address.city == \"Warsaw\""));
        assertEquals(".address.city", node.path().pathValue());
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
    void throwsForMissingOperatorAfterPath() {
        assertThrows(QueryParserException.class, () -> parse(".age"));
    }

    @Test
    void throwsForMissingClosingParen() {
        assertThrows(QueryParserException.class, () -> parse("(.age == 25"));
    }

    @Test
    void throwsForUnexpectedToken() {
        assertThrows(QueryParserException.class, () -> parse("== 25"));
    }
}
