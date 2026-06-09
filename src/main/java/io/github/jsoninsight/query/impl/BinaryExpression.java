package io.github.jsoninsight.query.impl;

import io.github.jsoninsight.query.QueryExpression;
import io.github.jsoninsight.query.QueryExpressionVisitor;

public class BinaryExpression implements QueryExpression {

    public enum Operator {
        AND, OR,
        EQ, NEQ,
        LT, LTE, GT, GTE
    }

    private final Operator operator;
    private final QueryExpression left;
    private final QueryExpression right;

    public BinaryExpression(QueryExpression left, Operator operator, QueryExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public QueryExpression getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public QueryExpression getRight() {
        return right;
    }

    @Override
    public boolean accept(QueryExpressionVisitor visitor) {
        return visitor.visitBinary(this);
    }
}
