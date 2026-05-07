package io.github.jsoninsight.query.evaluator.values;

import io.github.jsoninsight.query.evaluator.QueryValue;

public record StringValue(String value) implements QueryValue {
    @Override
    public String asString() {
        return value;
    }

    @Override
    public int size() {
        return value.length();
    }
}
