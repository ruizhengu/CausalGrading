package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
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
        buildSkeletonGraph();
        graph.generate("Cafe.dot");
    }

    private static void graphBuild(File file, String startNode, String lastMethod) throws FileNotFoundException {
        cu = StaticJavaParser.parse(file);
        // Identify all the method declarations in the file
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                String methodDeclarationClassMethod = Util.getLastSegment(n.resolve().getQualifiedName(), 2);
                if (!methodDeclarationClassMethod.equals(startNode)) {
                    return;
                }
                // Check data dependency in object creations
                new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ObjectCreationExpr o, Void arg) {
                        if (o.resolve().getPackageName().contains(PACKAGE_NAME)) {
                            graph.addNodeAndEdge(startNode, o.resolve().getName(), null);
                            if (o.getArguments().size() > 0) {
                                for (Expression argument : o.getArguments()) {
                                    addArgumentDependence(argument, startNode);
                                }
                            }
                        }
                    }
                }.visit(cu, null);
                // Identify all the method calls in the current declared method
                new VoidVisitorAdapter<Void>() {

//                    @Override
//                    public void visit(ObjectCreationExpr o, Void arg) {
//                        if (o.resolve().getPackageName().contains(PACKAGE_NAME)) {
//                            graph.addNodeAndEdge(startNode, o.resolve().getName(), null);
//                            if (o.getArguments().size() > 0) {
//                                for (Expression argument : o.getArguments()) {
//                                    addArgumentDependence(argument, startNode);
//                                }
//                            }
//                        }
//                    }

                    @Override
                    public void visit(MethodCallExpr m, Void arg) {
                        String methodCallClassMethod = Util.getLastSegment(m.resolve().getQualifiedName(), 2);
                        try {
                            if (m.resolve().getPackageName().contains(PACKAGE_NAME)) {
                                if (m.resolve().getQualifiedName().equals(lastMethod)) {
                                    return;
                                }
                                graph.addNodeAndEdge(startNode, methodCallClassMethod, null);
                                // Data dependency in the arguments passed in method calls
                                if (m.getArguments().size() > 0) {
                                    for (Expression argument : m.getArguments()) {
                                        addArgumentDependence(argument, methodCallClassMethod);
                                    }
                                }
                                graphBuild(Util.getFileOfMethod(m), methodCallClassMethod, m.resolve().getQualifiedName());
                            }
                            // If the arguments are method calls
                            else if (m.getArguments().stream().anyMatch(a -> a instanceof MethodCallExpr)) {
                                for (Expression argument : m.getArguments()) {
                                    if (argument instanceof MethodCallExpr) {
                                        String argumentMethodNode = Util.getLastSegment(((MethodCallExpr) argument).resolve().getQualifiedName(), 2);
                                        graph.addNodeAndEdge(methodDeclarationClassMethod, argumentMethodNode, null);
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

    private static void buildSkeletonGraph() throws FileNotFoundException {
        for (File file : FILES) {
            cu = StaticJavaParser.parse(file);
            addClassDeclaration(cu);
            addMethodDeclaration(cu);
        }
    }

    private static void addMethodDeclaration(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                graph.addNodeIfNotExists(String.join(".", m.resolve().getClassName(), m.resolve().getName()));
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

//    private static void addNodeAndEdge(String startNode, String endNode) {
//        if (!graph.nodeExists(startNode)) {
//            graph.addNode(startNode);
//        }
//        if (!graph.nodeExists(endNode)) {
//            graph.addNode(endNode);
//        }
//        if (!graph.edgeExists(startNode, endNode)) {
//            graph.link(startNode, endNode);
//        }
//    }

    private static void addArgumentDependence(Expression argument, String startNode) {
        String argumentNode;
        if (argument.calculateResolvedType().describe().contains(PACKAGE_NAME)) {
            if (argument instanceof MethodCallExpr) {
                // If argument is a method call expression
                argumentNode = Util.getLastSegment(((MethodCallExpr) argument).resolve().getQualifiedName(), 2);
            } else if (argument.isObjectCreationExpr()) {
                // If argument is a new object creation expression
                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe());
            } else if (argument.isFieldAccessExpr()) {
                // If argument is an instance variable
                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe(), 2, 1);
            } else {
                // If argument is an object reference
                argumentNode = Util.getLastSegment(argument.calculateResolvedType().describe(), 1);
            }
            graph.addNodeAndEdge(startNode, argumentNode, null);
        }
    }

    private static void addVariableDependence() {

    }
}
