package org.example;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.google.common.graph.Graph;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class DataDependence {

    public static JSONObject dependence = new JSONObject();
    public static String CLASS_KEY = "class";
    public static String ASSIGN_KEY = "assign";
    public static String ACCESS_KEY = "access";

    public static List<String> getTrace() throws FileNotFoundException {
        File log = new File("/home/ruizhen/Projects/CausalGrading/src/main/java/org/example/aspect/log.txt");
        List<String> executionTrace = new ArrayList<>();
        Scanner scanner = new Scanner(log);
        while (scanner.hasNextLine()) {
            String method = scanner.nextLine();
            if (!executionTrace.contains(method)) {
                executionTrace.add(Util.getLastSegment(method, 2));
            }
        }
        System.out.println(executionTrace);
        scanner.close();
        return executionTrace;
    }

    public void addObjectFields(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration c, Void arg) {
                for (ResolvedFieldDeclaration field : c.resolve().getAllFields()) {
                    JSONObject tmp = new JSONObject();
                    tmp.put(CLASS_KEY, c.getName());
                    dependence.put(field.getName(), tmp);
                }
            }
        }.visit(cu, null);
    }

    public void addDataDependence(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                String methodName = String.join(".", m.resolve().getClassName(), m.getNameAsString());
                new VoidVisitorAdapter<Void>() {
                    /** Record the method name if an object field variable is assigned in this method
                     * @param a ignore
                     * @param arg ignore
                     */
                    @Override
                    public void visit(AssignExpr a, Void arg) {
                        String key = a.getTarget().toString();
                        if (a.getTarget().isArrayAccessExpr()) {
                            key = a.getTarget().asArrayAccessExpr().getName().toString();
                        } else if (a.getTarget().isFieldAccessExpr()) {
                            key = a.getTarget().asFieldAccessExpr().getNameAsString();
                        } else {
                            System.out.println("Ignored Assign Expression: " + a);
                        }
                        appendDependence(key, ASSIGN_KEY, methodName);
                    }

                    /** Record the method name if an object field is increased or decreased
                     *  e.g. nRecipes++;
                     * @param u ignore
                     * @param arg ignore
                     */
                    @Override
                    public void visit(UnaryExpr u, Void arg) {
                        String key = u.getExpression().toString();
                        appendDependence(key, ASSIGN_KEY, methodName);
                    }

                    /**
                     * Record the method name if an object field variable is accessed in this method
                     * @param f ignore
                     * @param arg ignore
                     */
                    @Override
                    public void visit(FieldAccessExpr f, Void arg) {
                        String key = f.getScope().toString();
                        if (dependence.has(f.getScope().toString())) {
                            // If the scope of a Field Access Expression is identified object field variable
                            if (dependence.getJSONObject(key).get(CLASS_KEY).toString().equals(m.resolve().getClassName())) {
                                appendDependence(key, ACCESS_KEY, methodName);

                            }
                        } else if (dependence.has(f.getNameAsString())) {
                            key = f.getNameAsString();
                            // If is This Expression (e.g. int a = this.age;)
                            if (f.getScope().isThisExpr()) {
                                appendDependence(key, ASSIGN_KEY, methodName);
                            }
                        } else {
                            System.out.println("Ignored Field Access Expression: " + f);
                        }
                    }

                    /**Check if an object field variable is assigned to a local variable
                     *
                     * @param n ignore
                     * @param arg ignore
                     */
                    public void visit(NameExpr n, Void arg) {
                    }

                    /**Check if a variable is declared by an object field variable
                     *
                     * @param v ignore
                     * @param arg ignore
                     */
                    public void visit(VariableDeclarationExpr v, Void arg) {
                    }

                }.visit(m, null);
            }
        }.visit(cu, null);
    }

    public void appendDependence(String dependenceKey, String appendKey, String appendValue) {
        if (dependence.has(dependenceKey)) {
            JSONObject tmp = dependence.getJSONObject(dependenceKey);
            tmp.append(appendKey, appendValue);
            dependence.put(dependenceKey, tmp);
        }
    }

    /**
     * Connect the method nodes with data dependence
     *
     * @param graph The Digraph object
     */
    public void buildGraph(Digraph graph) throws FileNotFoundException {
        List<String> trace = getTrace();
        Iterator<String> keys = dependence.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject tmp = dependence.getJSONObject(key);
            if (tmp.has(ASSIGN_KEY) && tmp.has(ACCESS_KEY)) {
                for (Object methodAssign : tmp.getJSONArray(ASSIGN_KEY)) {
                    int indexAssign = trace.indexOf(methodAssign.toString());
                    for (Object methodAccess : tmp.getJSONArray(ACCESS_KEY)) {
                        int indexAccess = trace.indexOf(methodAccess.toString());
                        if (indexAssign > -1 && indexAccess > -1 && indexAccess > indexAssign) {
                            graph.addNodeAndEdge(methodAssign.toString(), methodAccess.toString(), Digraph.STYLE_DATA);
                        }
                    }
                }
            }
        }
    }

    public JSONObject getDependence() {
        return dependence;
    }
}
