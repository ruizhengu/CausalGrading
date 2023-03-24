package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
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
        File entry = getEntry();
        graph.addNode(ENTRY_NODE);
        graphBuild(entry, ENTRY_NODE, null);
        graph.generate("Cafe.dot");
    }

    private static void graphBuild(File file, String startNode, String lastMethod) throws FileNotFoundException {
        cu = StaticJavaParser.parse(file);

        // Identify all the method declarations in the file
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                String methodDeclarationClassMethod = Util.getClassMethod(n.resolve().getQualifiedName());
                if (!methodDeclarationClassMethod.equals(startNode)) {
                    return;
                }
                // Check data dependency in object creations
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ObjectCreationExpr o, Void arg) {
                        if (o.resolve().getPackageName().contains(PACKAGE_NAME)) {
                            // The type of the object created
                            addNodeAndEdge(startNode, o.resolve().getName());
                            // The types of arguments passed in the object creation expression
                            if (o.getArguments().size() > 0) {
                                for (Expression argument : o.getArguments()) {
                                    if (argument.calculateResolvedType().describe().contains(PACKAGE_NAME)) {
                                        addNodeAndEdge(startNode, Util.getClassMethod(argument.calculateResolvedType().describe()));
                                    }
                                }
                            }
                        }
                    }
                }.visit(cu, null);
                // Identify all the method calls in the current declared method
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodCallExpr m, Void arg) {
                        String methodCallClassMethod = Util.getClassMethod(m.resolve().getQualifiedName());
                        try {
                            // The method call itself is a method created in the project
                            if (m.resolve().getPackageName().contains(PACKAGE_NAME)) {
                                if (m.resolve().getQualifiedName().equals(lastMethod)) {
                                    return;
                                }
                                addNodeAndEdge(startNode, methodCallClassMethod);
                                // Data dependency
                                if (m.getArguments().size() > 0) {
                                    for (Expression argument : m.getArguments()) {
                                        if (argument instanceof MethodCallExpr) {
                                            String argumentMethodNode = Util.getClassMethod(((MethodCallExpr) argument).resolve().getQualifiedName());
                                            addNodeAndEdge(methodCallClassMethod, argumentMethodNode);
                                        } else {
                                            String argumentNode = Util.getClassMethod(argument.calculateResolvedType().describe());
                                            addNodeAndEdge(methodCallClassMethod, argumentNode);
                                        }
                                    }
                                }
                                graphBuild(Util.getFileOfMethod(m), methodCallClassMethod, m.resolve().getQualifiedName());
                            }
                            // If the arguments are method calls
                            else if (m.getArguments().stream().anyMatch(a -> a instanceof MethodCallExpr)) {
                                for (Expression argument : m.getArguments()) {
                                    if (argument instanceof MethodCallExpr) {
                                        String argumentMethodNode = Util.getClassMethod(((MethodCallExpr) argument).resolve().getQualifiedName());
                                        addNodeAndEdge(methodDeclarationClassMethod, argumentMethodNode);
                                        m = (MethodCallExpr) argument;
                                        graphBuild(Util.getFileOfMethod(m), methodCallClassMethod, m.resolve().getQualifiedName());
                                    }
                                }
                            }
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.visit(n, null);
            }
        }.visit(cu, null);


    }

    private static File getEntry() throws FileNotFoundException {
        for (File file : FILES) {
            cu = StaticJavaParser.parse(file);
            String entry_class = new GenericVisitorAdapter<String, Void>() {
                @Override
                public String visit(ClassOrInterfaceDeclaration n, Void arg) {
                    super.visit(n, arg);
                    for (MethodDeclaration method : n.getMethods()) {
                        if (String.valueOf(method.getName()).equals("main")) {
                            return String.valueOf(n.getName());
                        }
                    }
                    return null;
                }
            }.visit(cu, null);
            Pattern pattern = Pattern.compile(String.format(".*/%s.java", entry_class));
            Matcher matcher = pattern.matcher(file.toString());
            if (matcher.find()) {
                return file;
            }
        }
        throw new FileNotFoundException(String.format("Cannot find the class that has the method '%s'", ENTRY_NODE));
    }

    private static void addNodeAndEdge(String startNode, String endNode) {
        if (!graph.nodeExists(endNode)) {
            graph.addNode(endNode);
            graph.link(startNode, endNode);
        } else if (!graph.edgeExists(startNode, endNode)) {
            graph.link(startNode, endNode);
        }
    }
}
