package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.graph.Graph;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class App {
    public static String DIR_PATH;
    public static String PACKAGE_NAME = "uk.ac.sheffield.com1003.cafe";
    /**
     * Solid lines in the graph are method calls
     * Dashed lines in the graph are class dependence
     * Dotted lines in the graph are data dependence
     */
    public static Digraph graph = new Digraph("Cafe");
    public static CompilationUnit cu;
    public static JSONObject dependency = new JSONObject();
    public static String CLASS_KEY = "class";
    public static String ASSIGN_KEY = "assign";
    public static String ACCESS_KEY = "access";

    public static void main(String[] args) throws FileNotFoundException {
        DIR_PATH = Util.getOSPath();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        Set<File> files = Util.getFiles(new File(DIR_PATH));
        buildGraph(files);
        graph.generate("Cafe.dot");
    }

    private static void buildGraph(Set<File> files) throws FileNotFoundException {
        // Get all the object fields
        for (File file : files) {
            cu = StaticJavaParser.parse(file);
            addClasses(cu);
        }
        System.out.println(dependency);
        // Construct call graph and data dependency graph
        for (File file : files) {
            cu = StaticJavaParser.parse(file);
            addMethods(cu);
        }
        System.out.println(dependency);
    }

    private static void addMethods(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                String callerNode = String.join(".", m.resolve().getClassName(), m.getNameAsString());
//                System.out.println(callerNode);
                graph.addNodeIfNotExists(callerNode);
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodCallExpr n, Void arg) {
                        String calleeNode = String.join(".", n.resolve().getClassName(), n.getNameAsString());
                        // If the method call belongs to a class in the project
                        if (n.resolve().getQualifiedName().contains(PACKAGE_NAME)) {
                            graph.addNodeAndEdge(callerNode, calleeNode);
                        }
                        // If one of the arguments passed to a method is a method call
                        for (Expression argument : n.getArguments()) {
                            if (argument instanceof MethodCallExpr) {
                                addArgumentDependence(argument.asMethodCallExpr(), callerNode);
                            }
                        }
                    }

                    @Override
                    public void visit(AssignExpr v, Void arg) {
                        String key = v.getTarget().toString();
                        if (v.getTarget() instanceof ArrayAccessExpr) {
                            key = v.getTarget().asArrayAccessExpr().getName().toString();
                        }
                        if (v.getTarget() instanceof FieldAccessExpr) {
                            key = v.getTarget().asFieldAccessExpr().getNameAsString();
                        }
                        if (dependency.has(key)) {
                            JSONObject tmp = dependency.getJSONObject(key);
                            tmp.append(ASSIGN_KEY, callerNode);
                            dependency.put(key, tmp);
                        }
                    }

                    @Override
                    public void visit(UnaryExpr u, Void arg) {
                        String key = u.getExpression().toString();
                        if (dependency.has(key)) {
                            JSONObject tmp = dependency.getJSONObject(key);
                            tmp.append(ASSIGN_KEY, callerNode);
                            dependency.put(key, tmp);
                        }
                    }

//                    @Override
//                    public void visit(FieldAccessExpr f, Void arg) {
//                        System.out.println("Field Access: " + f.getName());
//                    }

                    // Object creations in the class
//                    @Override
//                    public void visit(ObjectCreationExpr o, Void arg) {
//                        if (o.resolve().getQualifiedName().contains(PACKAGE_NAME)) {
//                            graph.addNodeAndEdge(callerNode, o.getTypeAsString(), Digraph.STYLE_CLASS);
//                            for (Expression argument : o.getArguments()) {
//                                addArgumentDependence(argument, callerNode);
//                            }
//                        } else if (o.getArguments().stream().anyMatch(a -> a instanceof MethodCallExpr)) {
//                            for (Expression argument : o.getArguments()) {
//                                addArgumentDependence(argument, callerNode);
//                            }
//                        }
//                    }
                }.visit(m, null);
            }
        }.visit(cu, null);
    }

    /**
     * Add the classes which extend or implement abstract classes or interfaces
     *
     * @param cu The CompilationUnit of the file under analysis
     */
    private static void addClasses(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration c, Void arg) {
//                // inheritance of abstract class
//                for (ClassOrInterfaceType type : c.getExtendedTypes()) {
//                    if (type.resolve().describe().contains(PACKAGE_NAME)) {
//                        graph.addNodeAndEdge(c.getNameAsString(), type.getNameAsString(), Digraph.STYLE_CLASS);
//                    }
//                }
//                // inheritance of interface
//                for (ClassOrInterfaceType type : c.getImplementedTypes()) {
//                    if (type.resolve().describe().contains(PACKAGE_NAME)) {
//                        graph.addNodeAndEdge(c.getNameAsString(), type.getNameAsString(), Digraph.STYLE_CLASS);
//                    }
//                }

                // Add the fields and their classed in JSON object
                for (ResolvedFieldDeclaration field : c.resolve().getAllFields()) {
                    JSONObject desc = new JSONObject();
                    desc.put(CLASS_KEY, c.getName());
                    dependency.put(field.getName(), desc);
                }
            }
        }.visit(cu, null);
    }

    private static void addArgumentDependence(MethodCallExpr expr, String startNode) {
        // If the method call is defined in the project
        if (expr.getScope().isPresent()) {
            String className = expr.getScope().get().calculateResolvedType().describe();
            if (className.contains(PACKAGE_NAME)) {
                String argumentNode = String.join(".", Util.getLastSegment(className), expr.getNameAsString());
                graph.addNodeAndEdge(startNode, argumentNode);
            }
        }
//            } else if (argument.isObjectCreationExpr()) {
//                // If argument is a new object creation expression
//                argumentNode = argument.asObjectCreationExpr().resolve().getClassName();
//                graph.addNodeAndEdge(startNode, argumentNode, Digraph.STYLE_CLASS);
//            } else if (argument.isFieldAccessExpr()) {
//                // If argument is an instance variable
//                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe(), 2, 1);
//                graph.addNodeAndEdge(startNode, argumentNode, Digraph.STYLE_DATA);
//            } else {
//                // If argument is an object reference
//                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe());
//                graph.addNodeAndEdge(startNode, argumentNode, Digraph.STYLE_DATA);
//        }
    }

//    private static boolean checkDependence(Expression argument) {
//        return argument instanceof MethodCallExpr;
//        else if (argument.isObjectCreationExpr()) return true;
//        else if (argument.isFieldAccessExpr()) return true;
//        else return argument.isNameExpr();
//    }

    private static void addVariableDependence(MethodDeclaration method) {
        System.out.println(method.findAll(VariableDeclarator.class).stream());
    }
}
