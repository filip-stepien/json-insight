package io.github.jsoninsight.query.evaluator.values;

import io.github.jsoninsight.query.evaluator.QueryValue;

import java.math.BigDecimal;

public record NumberValue(BigDecimal value) implements QueryValue {
    @Override
    public BigDecimal asNumber() {
        return value;
    }
}
