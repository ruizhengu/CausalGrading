package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class App {

    public static String DIR_PATH;
    public static String PACKAGE_NAME = "uk.ac.sheffield.com1003.cafe";
    // The method used as the entry
    public static String ENTRY_NODE = "App.main";
    public static Digraph graph = new Digraph("Cafe");
    public static CompilationUnit cu;
    public static Set<File> FILES;


    public static void main(String[] args) throws FileNotFoundException {
        DIR_PATH = Util.getOSPath();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        FILES = Util.getFiles(new File(DIR_PATH));
//        File entry = getEntry();
//        graph.addNode(ENTRY_NODE);
//        graphBuild(entry, ENTRY_NODE, null);
        buildGraph();
        graph.generate("Cafe.dot");
    }


    private static void buildGraph() throws FileNotFoundException {
        for (File file : FILES) {
            cu = StaticJavaParser.parse(file);
            addClassDeclaration(cu);
            addMethods(cu);
        }
    }

    private static void addMethods(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                graph.addNodeIfNotExists(String.join(".", m.resolve().getClassName(), m.getNameAsString()));
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodCallExpr n, Void arg) {
                        String callerNode = String.join(".", m.resolve().getClassName(), m.getNameAsString());
                        String calleeNode = String.join(".", n.resolve().getClassName(), n.getNameAsString());
                        // If the method call belongs to a class in the project
                        if (n.resolve().getQualifiedName().contains(PACKAGE_NAME)) {
                            graph.addNodeAndEdge(callerNode, calleeNode);
                            for (Expression argument : n.getArguments()) {
                                addArgumentDependence(argument, calleeNode);
                            }
                        }
                        // If one of the arguments passed to a method is a method call, an object creation, an instance variable or an object reference
                        else if (n.getArguments().stream().anyMatch(App::checkDependence)) {
                            for (Expression argument : n.getArguments()) {
                                addArgumentDependence(argument, calleeNode);
                            }
                        }
                    }
                }.visit(m, null);
            }
        }.visit(cu, null);
    }

    /**
     * Add the classes which extend or implement abstract classes or interfaces
     *
     * @param cu The CompilationUnit of the file under analysis
     */
    private static void addClassDeclaration(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration c, Void arg) {
                // inheritance of abstract class
                for (ClassOrInterfaceType type : c.getExtendedTypes()) {
                    if (type.resolve().describe().contains(PACKAGE_NAME)) {
                        graph.addNodeAndEdge(c.getNameAsString(), type.getNameAsString(), Digraph.STYLE_DASH);
                    }
                }
                // inheritance of interface
                for (ClassOrInterfaceType type : c.getImplementedTypes()) {
                    if (type.resolve().describe().contains(PACKAGE_NAME)) {
                        graph.addNodeAndEdge(c.getNameAsString(), type.getNameAsString(), Digraph.STYLE_DASH);
                    }
                }
            }
        }.visit(cu, null);
    }

    private static void addArgumentDependence(Expression argument, String startNode) {
        String argumentNode;
        if (argument.calculateResolvedType().describe().contains(PACKAGE_NAME)) {
            System.out.println(argument);
            if (argument instanceof MethodCallExpr expr) {
                // If argument is a method call expression
                argumentNode = String.join(".", expr.resolve().getClassName(), expr.getNameAsString());
            } else if (argument.isObjectCreationExpr()) {
                // If argument is a new object creation expression
                argumentNode = argument.asObjectCreationExpr().resolve().getClassName();
            } else if (argument.isFieldAccessExpr()) {
                // If argument is an instance variable
                FieldAccessExpr expr = argument.asFieldAccessExpr();
                System.out.println("isFieldAccessExpr: " + expr.getName());
                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe(), 2, 1);
            } else {
                // If argument is an object reference
                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe());
            }
            graph.addNodeAndEdge(startNode, argumentNode, null);
        }
    }

    private static boolean checkDependence(Expression argument) {
        if (argument instanceof MethodCallExpr) return true;
        else if (argument.isObjectCreationExpr()) return true;
        else if (argument.isFieldAccessExpr()) return true;
        else return argument.isNameExpr();
    }

    private static void addVariableDependence() {

    }

    private static void addObjectCreation() {

    }
}
