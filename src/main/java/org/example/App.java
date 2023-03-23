package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static String DIR_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java";
    // The method used as the entry
    public static String ENTRY_NODE = "main";
    public static Digraph graph = new Digraph("Cafe");
    public static CompilationUnit cu;
    public static Set<File> FILES;

    public static void main(String[] args) throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        FILES = Util.getFiles(new File(DIR_PATH));
        File entry = getEntry();
    }

    private static void graphBuild() {

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
