package org.example;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;

import java.util.List;

public class Argument {

    public static boolean checkArguments(Expression argument) {
        return argument.isMethodCallExpr();
    }

    /**
     * If one of the argument of a method call is another method call expression, connect these two methods in the method call graph.
     *
     * @param expr      argument
     * @param graph     digraph
     * @param startNode parent method node
     */
    public static void addArgumentMethodCall(Expression expr, Digraph graph, String startNode) {
        // If the argument is a method call expression
        if (expr.isMethodCallExpr() && expr.asMethodCallExpr().getScope().isPresent()) {
            String className = expr.asMethodCallExpr().getScope().get().calculateResolvedType().describe();
            if (className.contains(Util.PACKAGE_NAME)) {
                String argumentNode = String.join(".", Util.getLastSegment(className), expr.asMethodCallExpr().getNameAsString());
                graph.addNodeAndEdge(startNode, argumentNode);
            }
        }
    }
}
