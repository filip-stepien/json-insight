package io.github.jsoninsight.query.evaluator.impl;

import io.github.jsoninsight.json.JsonNode;
import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.QueryExpressionVisitor;
import io.github.jsoninsight.query.ast.QueryLiteralNode;
import io.github.jsoninsight.query.ast.node.*;
import io.github.jsoninsight.query.ast.operator.ComparisonOperator;
import io.github.jsoninsight.query.evaluator.QueryEvaluator;
import io.github.jsoninsight.query.evaluator.QueryEvaluatorException;

import java.math.BigDecimal;
import java.util.Optional;

public class QueryEvaluatorImpl implements QueryEvaluator, QueryExpressionVisitor<Boolean> {

    private final QueryExpressionNode query;
    private JsonNode document;

    public QueryEvaluatorImpl(QueryExpressionNode query) {
        this.query = query;
    }

    private boolean applyOperator(int compareResult, ComparisonOperator operator) {
        return switch (operator) {
            case EQ -> compareResult == 0;
            case NEQ -> compareResult != 0;
            case GT -> compareResult > 0;
            case GTE -> compareResult >= 0;
            case LT -> compareResult < 0;
            case LTE -> compareResult <= 0;
        };
    }

    private boolean compareString(JsonNode value, ComparisonOperator operator, String literal) {
        if (!(value instanceof JsonNode.StringNode(String documentStringValue))) {
            return false;
        }

        return switch (operator) {
            case EQ -> documentStringValue.equals(literal);
            case NEQ -> !documentStringValue.equals(literal);
            default -> throw new QueryEvaluatorException(
                "Operator " + operator + " is not supported for string values"
            );
        };
    }

    private boolean compareNumber(JsonNode value, ComparisonOperator operator, BigDecimal literal) {
        if (!(value instanceof JsonNode.NumberNode numberNode)) {
            return false;
        }

        int compareResult = new BigDecimal(numberNode.raw()).compareTo(literal);
        return applyOperator(compareResult, operator);
    }

    private boolean compareBoolean(JsonNode value, ComparisonOperator operator, boolean literal) {
        if (!(value instanceof JsonNode.BooleanNode(boolean documentBooleanValue))) {
            return false;
        }

        return switch (operator) {
            case EQ -> documentBooleanValue == literal;
            case NEQ -> documentBooleanValue != literal;
            default -> throw new QueryEvaluatorException(
                "Operator " + operator + " is not supported for boolean values"
            );
        };
    }

    private boolean compareNull(JsonNode value, ComparisonOperator operator) {
        boolean isNull = value instanceof JsonNode.NullNode;

        return switch (operator) {
            case EQ -> isNull;
            case NEQ -> !isNull;
            default -> throw new QueryEvaluatorException(
                "Operator " + operator + " is not supported for null values"
            );
        };
    }

    private boolean compare(JsonNode value, ComparisonOperator operator, QueryLiteralNode literal) {
        return switch (literal) {
            case StringLiteralNode stringLiteral -> compareString(value, operator, stringLiteral.value());
            case NumberLiteralNode numberLiteral -> compareNumber(value, operator, numberLiteral.value());
            case BooleanLiteralNode booleanLiteral -> compareBoolean(value, operator, booleanLiteral.value());
            case NullLiteralNode ignored -> compareNull(value, operator);
        };
    }

    private String[] splitIntoPathSegments(JsonPathNode path) {
        String pathWithoutLeadingDot = path.pathValue().substring(1);
        return pathWithoutLeadingDot.split("\\.");
    }

    private Optional<JsonNode> resolveNextPathSegment(JsonNode current, String segment) {
        if (current instanceof JsonNode.ObjectNode objectNode) {
            JsonNode nextNode = objectNode.fields().get(segment);
            return Optional.ofNullable(nextNode);
        }

        return Optional.empty();
    }

    private Optional<JsonNode> resolvePath(JsonPathNode path) {
        String[] segments = splitIntoPathSegments(path);
        JsonNode current = document;

        for (String segment : segments) {
            Optional<JsonNode> next = resolveNextPathSegment(current, segment);

            if (next.isEmpty()) {
                return Optional.empty();
            }

            current = next.get();
        }

        return Optional.of(current);
    }

    @Override
    public boolean evaluate(JsonNode document) {
        this.document = document;
        return query.accept(this);
    }

    @Override
    public Boolean visitLogical(LogicalNode node) {
        return switch (node.operator()) {
            case AND -> node.leftExpression().accept(this) && node.rightExpression().accept(this);
            case OR -> node.leftExpression().accept(this) || node.rightExpression().accept(this);
        };
    }

    @Override
    public Boolean visitNot(NotNode node) {
        return !node.operand().accept(this);
    }

    @Override
    public Boolean visitExists(ExistsNode node) {
        return resolvePath(node.path()).isPresent();
    }

    @Override
    public Boolean visitComparison(ComparisonNode node) {
        Optional<JsonNode> resolved = resolvePath(node.path());
        return resolved.filter(
            jsonNode -> compare(jsonNode, node.operator(), node.rightValue())
        ).isPresent();
    }

    @Override
    public Boolean visitIs(IsNode node) {
        throw new UnsupportedOperationException("visitIs not implemented yet");
    }

    @Override
    public Boolean visitFunctionCall(FunctionCallNode node) {
        throw new UnsupportedOperationException("visitFunctionCall not implemented yet");
    }
}
