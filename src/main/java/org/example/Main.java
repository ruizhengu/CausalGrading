package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithMembers;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import javax.swing.plaf.synth.SynthUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static String DIR_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java";
    public static String ENTRY_CLASS;
    public static Set<String> callee = new HashSet<>();

    public static String startingNode = "";
    public static JSONObject jsonObject = new JSONObject();

    public static CompilationUnit cu;

    public static void main(String[] args) throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

//        cu = StaticJavaParser.parse(new File(FILE_PATH));
//        cu.accept(new ClassVisitor(), null);
//        cu.accept(new MethodVisitor(), null);

//        jsonObject.put(startingNode, callee);
//        System.out.println(jsonObject);

        File entry = getEntry();
        System.out.println(entry);
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
            cu.accept(new ClassVisitor(), null);
        }
        for (File file : Util.listFiles(new File(DIR_PATH))) {
            Pattern pattern = Pattern.compile(".*/" + ENTRY_CLASS + ".java");
            Matcher matcher = pattern.matcher(file.toString());
            if (matcher.find()) {
                return file;
            }
        }
        return null;
    }


    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            System.out.println("===========");
//            System.out.println("Statement: " + n);
//            System.out.println("Method Call: " + n.getName());
//            System.out.println("Caller instance: " + n.getScope().get());
//            System.out.println("Class of instance: " + n.getScope().get().calculateResolvedType().describe());

            System.out.println(n.getParentNode());
            System.out.println(n.getName());
            if (!Objects.equals(n.getScope().toString(), "Optional.empty")) {
                callee.add(n.getScope().get().calculateResolvedType().describe() + "." + n.getName());
            }

            // Get arguments class name
            // For Data Dependency Graph - save this for later
//            if (n.getArguments().size() > 0) {
//                for (int i = 0; i < n.getArguments().size(); i++) {
//                    System.out.println("Argument " + i + ": " + n.getArguments().get(i));
//                    System.out.println("Argument " + i + " class: " + n.getArguments().get(i).calculateResolvedType().describe());
//                }
//            }
        }
    }

    private static class ClassVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            super.visit(n, arg);
            for (MethodDeclaration method : n.getMethods()) {
                if (String.valueOf(method.getName()).equals("main")) {
                    ENTRY_CLASS = String.valueOf(n.getName());
                    System.out.println(n.getName());
                }
            }
        }
    }
}