package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.ast.expression.operator.ComparisonOperator;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryExpressionEvaluatorException;
import io.github.jsoninsight.query.evaluator.QueryValue;
import io.github.jsoninsight.query.evaluator.values.ArrayValue;
import io.github.jsoninsight.query.evaluator.values.BooleanValue;
import io.github.jsoninsight.query.evaluator.values.NullValue;
import io.github.jsoninsight.query.evaluator.values.NumberValue;
import io.github.jsoninsight.query.evaluator.values.ObjectValue;
import io.github.jsoninsight.query.evaluator.values.StringValue;

import java.math.BigDecimal;

public record ComparisonNode(
    QueryExpression leftExpression,
    ComparisonOperator operator,
    QueryExpression rightExpression
) implements QueryExpression {

    private boolean applyOperator(int compareResult) {
        return switch (operator) {
            case EQ -> compareResult == 0;
            case NEQ -> compareResult != 0;
            case GT -> compareResult > 0;
            case GTE -> compareResult >= 0;
            case LT -> compareResult < 0;
            case LTE -> compareResult <= 0;
        };
    }

    private boolean compareString(String left, QueryValue rightValue) {
        if (!(rightValue instanceof StringValue(String right))) {
            return false;
        }

        return switch (operator) {
            case EQ -> left.equals(right);
            case NEQ -> !left.equals(right);
            default -> throw new QueryExpressionEvaluatorException(
                "Operator " + operator + " is not supported for string values"
            );
        };
    }

    private boolean compareNumber(BigDecimal left, QueryValue rightValue) {
        if (!(rightValue instanceof NumberValue(BigDecimal right))) {
            return false;
        }

        return applyOperator(left.compareTo(right));
    }

    private boolean compareBoolean(boolean left, QueryValue rightValue) {
        if (!(rightValue instanceof BooleanValue(boolean right))) {
            return false;
        }

        return switch (operator) {
            case EQ -> left == right;
            case NEQ -> left != right;
            default -> throw new QueryExpressionEvaluatorException(
                "Operator " + operator + " is not supported for boolean values"
            );
        };
    }

    private boolean compareAgainstNull(QueryValue leftValue) {
        return switch (operator) {
            case EQ -> leftValue instanceof NullValue;
            case NEQ -> !(leftValue instanceof NullValue);
            default -> throw new QueryExpressionEvaluatorException(
                "Operator " + operator + " is not supported for null values"
            );
        };
    }

    private boolean compare(QueryValue leftValue, QueryValue rightValue) {
        if (rightValue instanceof NullValue) {
            return compareAgainstNull(leftValue);
        }

        return switch (leftValue) {
            case StringValue(String left) -> compareString(left, rightValue);
            case NumberValue(BigDecimal left) -> compareNumber(left, rightValue);
            case BooleanValue(boolean left) -> compareBoolean(left, rightValue);
            case NullValue ignored -> false;
            case ArrayValue ignored -> false;
            case ObjectValue ignored -> false;
        };
    }

    @Override
    public QueryValue evaluate(EvaluationContext context) {
        QueryValue leftValue = leftExpression.evaluate(context);
        QueryValue rightValue = rightExpression.evaluate(context);
        return QueryValue.bool(compare(leftValue, rightValue));
    }
}
