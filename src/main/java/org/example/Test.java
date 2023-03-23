package org.example;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class Test {
    public static String DIR_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java";

    public static String FILE_PATH = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java/uk/ac/sheffield/com1003/cafe/Recipe.java";

    public static void main(String[] args) throws Exception {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_PATH));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));

        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                // Print the method name and signature
                ResolvedMethodDeclaration resolvedMethod = n.resolve();
                System.out.println("Method: " + resolvedMethod.getName());

                // Create a visitor to visit method call expressions
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodCallExpr m, Void arg) {
                        // Print the method name and signature being called
                        System.out.println("Method call: " + m.getNameAsString());
                        System.out.println("Method call class: " + m.getScope().get().calculateResolvedType().describe());
                    }
                }.visit(n, null);
            }
        }.visit(cu, null);
    }
}
