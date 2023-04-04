package org.example;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodCall {

    public static void addMethodCalls(CompilationUnit cu, Digraph graph) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                String callerNode = String.join(".", m.resolve().getClassName(), m.getNameAsString());
                graph.addNodeIfNotExists(callerNode);
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodCallExpr n, Void arg) {
                        String calleeNode = String.join(".", n.resolve().getClassName(), n.getNameAsString());
                        // If the method call belongs to a class in the project
                        if (n.resolve().getQualifiedName().contains(Util.PACKAGE_NAME)) {
                            graph.addNodeAndEdge(callerNode, calleeNode);
                        }
                        // If one of the arguments passed to a method is a method call
                        for (Expression argument : n.getArguments()) {
                            if (argument.isMethodCallExpr()) {
                                Argument.addArgumentDependence(argument, graph, callerNode);
                            }
                        }
                    }
                }.visit(m, null);
            }
        }.visit(cu, null);
    }

}