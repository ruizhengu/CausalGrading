package org.example;

import com.github.javaparser.ast.expr.Expression;

public class Argument {

    public static boolean checkArguments(Expression argument) {
        return argument.isMethodCallExpr();
    }

    public static void addArgumentDependence(Expression expr, Digraph graph, String startNode) {
        // If the argument is a method call expression
        if (expr.isMethodCallExpr() && expr.asMethodCallExpr().getScope().isPresent()) {
            String className = expr.asMethodCallExpr().getScope().get().calculateResolvedType().describe();
            if (className.contains(Util.PACKAGE_NAME)) {
                String argumentNode = String.join(".", Util.getLastSegment(className), expr.asMethodCallExpr().getNameAsString());
                graph.addNodeAndEdge(startNode, argumentNode);
            }
        }
        // If the argument is an object reference
    }
}
