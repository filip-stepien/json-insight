package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryValue;

import java.math.BigDecimal;

public record SizeNode(QueryExpression value) implements QueryExpression {
    @Override
    public QueryValue evaluate(EvaluationContext context) {
        return QueryValue.number(BigDecimal.valueOf(value.evaluate(context).size()));
    }
}
