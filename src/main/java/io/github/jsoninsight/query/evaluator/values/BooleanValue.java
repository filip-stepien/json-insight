package io.github.jsoninsight.query.evaluator.values;

import io.github.jsoninsight.query.evaluator.QueryValue;

public record BooleanValue(boolean value) implements QueryValue {
    @Override
    public boolean asBoolean() {
        return value;
    }
}
