package io.github.jsoninsight.query.evaluator.values;

import io.github.jsoninsight.query.evaluator.QueryValue;

import java.util.Map;

public record ObjectValue(Map<String, QueryValue> fields) implements QueryValue {
    @Override
    public int size() {
        return fields.size();
    }
}
