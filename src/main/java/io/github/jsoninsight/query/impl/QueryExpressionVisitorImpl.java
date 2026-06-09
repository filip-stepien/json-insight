package io.github.jsoninsight.query.impl;

import io.github.jsoninsight.query.QueryExpressionVisitor;

public class QueryExpressionVisitorImpl implements QueryExpressionVisitor {

    @Override
    public boolean visitBinary(BinaryExpression expression) {
        return switch (expression.getOperator()) {
            case AND -> expression.getLeft().accept(this) && expression.getRight().accept(this);
            case OR  -> expression.getLeft().accept(this) || expression.getRight().accept(this);
            case EQ  -> compare(expression) == 0;
            case NEQ -> compare(expression) != 0;
            case LT  -> compare(expression) < 0;
            case LTE -> compare(expression) <= 0;
            case GT  -> compare(expression) > 0;
            case GTE -> compare(expression) >= 0;
        };
    }

    @SuppressWarnings("unchecked")
    private int compare(BinaryExpression expression) {
        Object l = expression.getLeft().accept(this);
        Object r = expression.getRight().accept(this);
        return ((Comparable<Object>) l).compareTo(r);
    }
}
