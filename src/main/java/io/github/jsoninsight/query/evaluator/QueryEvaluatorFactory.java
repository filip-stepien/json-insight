package io.github.jsoninsight.query.evaluator;

import io.github.jsoninsight.query.evaluator.impl.QueryPredicateEvaluatorImpl;
import io.github.jsoninsight.query.evaluator.impl.QueryStatementEvaluatorImpl;

public class QueryEvaluatorFactory {

    public static QueryStatementEvaluator createStatementEvaluator() {
        return new QueryStatementEvaluatorImpl(new QueryPredicateEvaluatorImpl());
    }
}
