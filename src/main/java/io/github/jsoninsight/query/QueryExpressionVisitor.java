package io.github.jsoninsight.query;

import io.github.jsoninsight.query.impl.BinaryExpression;

public interface QueryExpressionVisitor {
    boolean visitBinary(BinaryExpression expression);
}
