package io.github.jsoninsight.query.ast.expression;

import io.github.jsoninsight.query.ast.expression.node.BooleanLiteralNode;
import io.github.jsoninsight.query.ast.expression.node.ComparisonNode;
import io.github.jsoninsight.query.ast.expression.node.ExistsNode;
import io.github.jsoninsight.query.ast.expression.node.IsNode;
import io.github.jsoninsight.query.ast.expression.node.JsonPathNode;
import io.github.jsoninsight.query.ast.expression.node.LogicalNode;
import io.github.jsoninsight.query.ast.expression.node.MatchesNode;
import io.github.jsoninsight.query.ast.expression.node.NotNode;
import io.github.jsoninsight.query.ast.expression.node.NullLiteralNode;
import io.github.jsoninsight.query.ast.expression.node.NumberLiteralNode;
import io.github.jsoninsight.query.ast.expression.node.SizeNode;
import io.github.jsoninsight.query.ast.expression.node.StringLiteralNode;
import io.github.jsoninsight.query.evaluator.EvaluationContext;
import io.github.jsoninsight.query.evaluator.QueryValue;

public sealed interface QueryExpression permits
    BooleanLiteralNode,
    ComparisonNode,
    ExistsNode,
    IsNode,
    JsonPathNode,
    LogicalNode,
    MatchesNode,
    NotNode,
    NullLiteralNode,
    NumberLiteralNode,
    SizeNode,
    StringLiteralNode {

    QueryValue evaluate(EvaluationContext context);
}
