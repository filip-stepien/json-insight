package io.github.jsoninsight.query.ast.node;

import io.github.jsoninsight.query.ast.QueryArgNode;
import io.github.jsoninsight.query.ast.QueryExpressionNode;
import io.github.jsoninsight.query.ast.QueryNodeVisitor;
import java.util.List;

public record FunctionCallNode(String functionName, List<QueryArgNode> args) implements QueryExpressionNode {
    @Override
    public <T> T accept(QueryNodeVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }
}
