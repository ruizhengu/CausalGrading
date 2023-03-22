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
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import javassist.compiler.ast.Pair;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static String DIR_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java";
    public static String ENTRY_NODE = "main";
    public static JSONObject jsonObject = new JSONObject();
    public static Digraph graph = new Digraph("Cafe");
    public static CompilationUnit cu;

    public static void main(String[] args) throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        graph.addNode(ENTRY_NODE);

        File entry = getEntry();
        System.out.println("Entry Class: " + entry);

        Set<String> callers = getCallers(entry);
        System.out.println(callers);
        graph.generate("Cafe.dot");
    }

    /**
     * Get the class with a "main" method as the entry class
     *
     * @return The file object of the entry class
     * @throws FileNotFoundException
     */
    public static File getEntry() throws FileNotFoundException {
        for (File file : Util.listFiles(new File(DIR_PATH))) {
            cu = StaticJavaParser.parse(file);
            String entry_class = cu.accept(new ClassVisitor(), null);
            Pattern pattern = Pattern.compile(".*/" + entry_class + ".java");
            Matcher matcher = pattern.matcher(file.toString());
            if (matcher.find()) {
                return file;
            }
        }
        return null;
    }

    public static Set<String> getCallers(File entry) throws FileNotFoundException {
        cu = StaticJavaParser.parse(entry);
        MethodVisitor methodVisitor = new MethodVisitor();
        cu.accept(methodVisitor, null);
        return methodVisitor.getCaller();
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {

        private static Set<String> caller = new HashSet<>();

        public Set<String> getCaller() {
            return caller;
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            // Add method calls in the graph
            String methodNode = String.valueOf(n.getName());
            if (!graph.exists(methodNode)) {
                graph.addNode(methodNode);
                graph.link(ENTRY_NODE, methodNode);
            }
            // Add arguments as data dependency in the graph
            if (n.getArguments().size() > 0) {
                for (Expression argument : n.getArguments()) {
                    String argumentNode = Util.getSimpleClassName(argument.calculateResolvedType().describe());
                    if (!graph.exists(argumentNode)) {
                        graph.addNode(argumentNode);
                        graph.link(methodNode, argumentNode);
                    }
                }
            }
        }
    }

    private static class ClassVisitor extends GenericVisitorAdapter<String, Void> {
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
    }
}