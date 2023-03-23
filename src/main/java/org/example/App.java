package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class App {

    public static String DIR_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java";
    public static String PACKAGE_NAME = "uk.ac.sheffield.com1003.cafe";
    // The method used as the entry
    public static String ENTRY_NODE = "main";
    public static Digraph graph = new Digraph("Cafe");
    public static CompilationUnit cu;
    public static String RECURSION_FLAG = null;
    public static Set<File> FILES;

    public static void main(String[] args) throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        FILES = Util.getFiles(new File(DIR_PATH));
        File entry = getEntry();
        graph.addNode(ENTRY_NODE);
        graphBuild(entry, ENTRY_NODE, null);
    }

    private static void graphBuild(File file, String startNode, String lastMethod) throws FileNotFoundException {
        cu = StaticJavaParser.parse(file);
        new VoidVisitorAdapter<Void>() {
            // Identify all the method declarations in the file
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                new VoidVisitorAdapter<Void>() {
                    // Identify all the method calls in the current declared method
                    @Override
                    public void visit(MethodCallExpr m, Void arg) {
                        try {
                            if (m.resolve().getPackageName().contains(PACKAGE_NAME)) {
                                if (m.resolve().getQualifiedName().equals(lastMethod)) {
                                    graph.generate("Cafe.dot");
                                    System.exit(0);
                                }
                                if (!graph.exists(m.getNameAsString())) {
                                    graph.addNode(m.getNameAsString());
                                    graph.link(startNode, m.getNameAsString());
                                }
                                if (m.getArguments().size() > 0) {
                                    for (Expression argument : m.getArguments()) {
                                        String fullyQualifiedName = argument.calculateResolvedType().describe();
                                        String argumentNode = fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
                                        if (!graph.exists(argumentNode)) {
                                            graph.addNode(argumentNode);
                                            graph.link(m.getNameAsString(), argumentNode);
                                        }
                                    }
                                }
                                graphBuild(Util.getFileOfMethod(m), m.getNameAsString(), m.resolve().getQualifiedName());
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
}