package io.github.jsoninsight.query.ast;

import io.github.jsoninsight.query.ast.node.ComparisonNode;
import io.github.jsoninsight.query.ast.node.ExistsNode;
import io.github.jsoninsight.query.ast.node.FunctionCallNode;
import io.github.jsoninsight.query.ast.node.IsNode;
import io.github.jsoninsight.query.ast.node.LogicalNode;
import io.github.jsoninsight.query.ast.node.NotNode;

public sealed interface QueryExpressionNode extends QueryNode permits
    ComparisonNode,
    LogicalNode,
    NotNode,
    ExistsNode,
    IsNode,
    FunctionCallNode {
}
