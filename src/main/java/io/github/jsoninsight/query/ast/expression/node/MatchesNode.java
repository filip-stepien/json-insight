package io.github.jsoninsight.query.ast.expression.node;

import io.github.jsoninsight.query.ast.expression.QueryExpression;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryExpressionEvaluatorException;
import io.github.jsoninsight.query.evaluator.QueryValue;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public record MatchesNode(QueryExpression value, QueryExpression pattern) implements QueryExpression {
    @Override
    public QueryValue evaluate(EvaluationContext context) {
        String text = value.evaluate(context).asString();
        String regex = pattern.evaluate(context).asString();

        try {
            return QueryValue.bool(Pattern.compile(regex).matcher(text).matches());
        } catch (PatternSyntaxException e) {
            throw new QueryExpressionEvaluatorException("Invalid regex pattern: " + regex);
        }
    }
}
