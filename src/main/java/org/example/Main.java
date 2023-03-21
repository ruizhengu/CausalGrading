package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static String FILE_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java/uk/ac/sheffield/com1003/cafe/App.java";
    public static String DIR_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java/";

    public static Set<String> callee = new HashSet<>();

    public static String startingNode = "";
    public static JSONObject jsonObject = new JSONObject();

    public static void main(String[] args) throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));
        cu.accept(new ClassVisitor(), null);


//        Set<String> set = new HashSet<String>();
//        set.add("coffee");
//        set.add("water");
//        System.out.println(set);
        jsonObject.put(startingNode, callee);
        System.out.println(jsonObject);
    }

    private static class ClassVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
//            System.out.println("===========");
//            System.out.println("Statement: " + n);
//            System.out.println("Method Call: " + n.getName());
//            System.out.println("Caller instance: " + n.getScope().get());
//            System.out.println("Class of instance: " + n.getScope().get().calculateResolvedType().describe());
            System.out.println(n.getName());
            System.out.println(n.getScope());
            callee.add(n.getScope().get().calculateResolvedType().describe() + "." + n.getName());


            // Get arguments class name
            // For Data Dependency Graph - save this for later
//            if (n.getArguments().size() > 0) {
//                for (int i = 0; i < n.getArguments().size(); i++) {
//                    System.out.println("Argument " + i + ": " + n.getArguments().get(i));
//                    System.out.println("Argument " + i + " class: " + n.getArguments().get(i).calculateResolvedType().describe());
//                }
//            }
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            super.visit(n, arg);
            startingNode = String.valueOf(n.getName());
        }

//        @Override
//        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
//            super.visit(n, arg);
//            System.out.println(n.getName());
//        }


    }
}