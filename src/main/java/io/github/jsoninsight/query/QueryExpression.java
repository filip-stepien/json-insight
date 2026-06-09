package io.github.jsoninsight.query;

public interface QueryExpression {
    boolean accept(QueryExpressionVisitor visitor);
}