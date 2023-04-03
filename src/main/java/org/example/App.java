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
    /**
     * Solid lines in the graph are method calls
     * Dashed lines in the graph are class dependence
     * Dotted lines in the graph are data dependence
     */
    public static Digraph graph = new Digraph("Cafe");
    public static CompilationUnit cu;
    public static DataDependence dataDependence;

    public static void main(String[] args) throws FileNotFoundException {
        DIR_PATH = Util.getOSPath();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        dataDependence = new DataDependence();
        Set<File> files = Util.getFiles(new File(DIR_PATH));
        buildGraph(files);
        graph.generate("Cafe.dot");
    }

    private static void buildGraph(Set<File> files) throws FileNotFoundException {
        // Get all the object fields
        for (File file : files) {
            cu = StaticJavaParser.parse(file);
            dataDependence.addObjectFields(cu);
        }
        System.out.println(dataDependence.getDependence());
        // Construct call graph and data dependency graph
        for (File file : files) {
            cu = StaticJavaParser.parse(file);
            MethodCall.addMethodCalls(cu, graph);
            dataDependence.addDataDependence(cu);
        }
        System.out.println(dataDependence.getDependence());
    }

    public static void getTrace() {

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
                // inheritance of abstract class
                for (ClassOrInterfaceType type : c.getExtendedTypes()) {
                    if (type.resolve().describe().contains(Util.PACKAGE_NAME)) {
                        graph.addNodeAndEdge(c.getNameAsString(), type.getNameAsString(), Digraph.STYLE_CLASS);
                    }
                }
                // inheritance of interface
                for (ClassOrInterfaceType type : c.getImplementedTypes()) {
                    if (type.resolve().describe().contains(Util.PACKAGE_NAME)) {
                        graph.addNodeAndEdge(c.getNameAsString(), type.getNameAsString(), Digraph.STYLE_CLASS);
                    }
                }
            }
        }.visit(cu, null);
    }
}
