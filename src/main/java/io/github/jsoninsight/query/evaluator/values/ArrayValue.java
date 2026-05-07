package io.github.jsoninsight.query.evaluator.values;

import io.github.jsoninsight.query.evaluator.QueryValue;

import java.util.List;

public record ArrayValue(List<QueryValue> values) implements QueryValue {
    @Override
    public int size() {
        return values.size();
    }
}
