package org.example;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class DataDependence {

    public static JSONObject dependence = new JSONObject();
    public static JSONObject constructors = new JSONObject();

    public static String FIELD_KEY = "field";
    // When an object field variable is assigned/updated
    public static String ASSIGN_KEY = "assign";
    // When an object field variable is accessed/used
    public static String ACCESS_KEY = "access";
    public static String PARAMETER_TYPE_KEY = "type";
    public static String PARAMETER_FIELD_KEY = "field";

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
//        System.out.println(executionTrace);
        scanner.close();
        return executionTrace;
    }

    /**
     * Record all the object field variables in all the classes
     *
     * @param cu ignore
     */
    public void addObjectFields(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration c, Void arg) {
                for (ResolvedFieldDeclaration field : c.resolve().getAllFields()) {
                    JSONObject tmp = new JSONObject();
                    tmp.put(FIELD_KEY, field.getName());
                    dependence.append(c.getNameAsString(), tmp);
                }
            }
        }.visit(cu, null);
    }

    /**
     * Record all the object constructors and the types of their parameters
     * Only covers the constructors that have parameters
     *
     * @param cu ignore
     */
    public void addConstructorParameters(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ConstructorDeclaration con, Void arg) {
                JSONArray arrayName = new JSONArray();
                JSONArray arrayType = new JSONArray();
                for (Parameter parameter : con.getParameters()) {
                    arrayType.put(parameter.getType().toString());
                    arrayName.put(parameter.getName().toString());
                }
                // Ignoring checking if all the parameters of a constructor are used to update object field variables

//                new VoidVisitorAdapter<Void>() {
//                    @Override
//                    public void visit(AssignExpr a, Void arg) {
//                        if (!arrayName.toList().contains(a.getValue().toString()) && !a.getTarget().isFieldAccessExpr()) {
//                            validFlag[0] = false;
//                        }
//                    }
//                }.visit(con, null);
                if (arrayType.length() != 0) {
                    JSONObject parameter = new JSONObject();
                    parameter.put(PARAMETER_TYPE_KEY, arrayType);
                    parameter.put(PARAMETER_FIELD_KEY, arrayName);
                    constructors.append(con.getNameAsString(), parameter);
                }
            }
        }.visit(cu, null);
    }

    public void addDataDependence(CompilationUnit cu) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                String className = m.resolve().getClassName();
                new VoidVisitorAdapter<Void>() {
                    /**
                     * Record the method name if an object field variable is assigned in this method
                     *
                     * @param a ignore
                     * @param arg ignore
                     */
                    @Override
                    public void visit(AssignExpr a, Void arg) {
                        String key = a.getTarget().toString();
                        String value = a.getValue().toString();
                        if (a.getTarget().isArrayAccessExpr()) {
                            // If the assign target is a list array type object field variable
                            ArrayAccessExpr arrayAccessExpr = a.getTarget().asArrayAccessExpr();
                            key = arrayAccessExpr.getName().toString();
                            appendDependence(key, ASSIGN_KEY, m);
                            // If the index of the assign target is an object field variable
                            String index = arrayAccessExpr.getIndex().toString();
                            appendDependence(index, ACCESS_KEY, m);
                        }
                        // An object field variable is assigned (e.g. a = 1)
                        else if (a.getTarget().isFieldAccessExpr()) {
                            key = a.getTarget().asFieldAccessExpr().getNameAsString();
                            appendDependence(key, ASSIGN_KEY, m);
                        } else if (a.getValue().isArrayAccessExpr()) {
                            // If the assign value is a list array type object field variable
                            ArrayAccessExpr arrayAccessExpr = a.getValue().asArrayAccessExpr();
                            key = arrayAccessExpr.getName().toString();
                            appendDependence(key, ACCESS_KEY, m);
                            // If the index of the assign value is an object field variable
                            String index = arrayAccessExpr.getIndex().toString();
                            appendDependence(index, ACCESS_KEY, m);
                        }
                        // if a local variable is assigned by an object field variable
                        else if (dependenceHasValue(className, value)) {
                            appendDependence(value, ACCESS_KEY, m);
                        } else if (dependenceHasValue(className, key)) {
                            appendDependence(key, ACCESS_KEY, m);
                        } else {
                            System.out.println("Ignored Field Assign Expression: " + a);
                        }
                    }

                    /**
                     * Record the method name if an object field is increased or decreased
                     *  e.g. nRecipes++;
                     *
                     * @param u ignore
                     * @param arg ignore
                     */
                    @Override
                    public void visit(UnaryExpr u, Void arg) {
                        String key = u.getExpression().toString();
                        appendDependence(key, ASSIGN_KEY, m);
                    }

                    /**
                     * Record the method name if an object field variable is accessed in this method
                     * @param f ignore
                     * @param arg ignore
                     */
                    @Override
                    public void visit(FieldAccessExpr f, Void arg) {
                        String key = f.getScope().toString();
                        if (dependenceHasValue(className, key)) {
                            appendDependence(key, ACCESS_KEY, m);
                        } else if (f.getScope().isThisExpr()) {
                            key = f.getNameAsString();
                            appendDependence(key, ACCESS_KEY, m);
                        } else {
                            System.out.println("Ignored Field Access Expression: " + f);
                        }
                    }

                    /**
                     * Check if the value assigned to a local variable when it's declared is an object field variable
                     *
                     * @param v ignore
                     * @param arg ignore
                     */
                    public void visit(VariableDeclarationExpr v, Void arg) {
                        for (VariableDeclarator variable : v.getVariables()) {
                            if (variable.getInitializer().isPresent()) {
                                Expression initializer = variable.getInitializer().get();
                                String value = initializer.toString();
                                appendDependence(value, ASSIGN_KEY, m);
                                if (initializer.isObjectCreationExpr()) {
                                    String objectClass = initializer.asObjectCreationExpr().getType().toString();
                                    if (constructors.has(objectClass)) {
                                        for (Object constructor : constructors.getJSONArray(objectClass)) {
                                            if (Util.matchArguments(initializer.asObjectCreationExpr().getArguments(), (JSONObject) constructor)) {
                                                for (Object key : ((JSONObject) constructor).getJSONArray(PARAMETER_FIELD_KEY)) {
                                                    appendDependence(key.toString(), ASSIGN_KEY, objectClass, m);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    /**
                     * If one of the arguments of a method call is an object field variable, add the method as the assign method of the variable.
                     *
                     * @param c ignore
                     * @param arg ignore
                     */
                    public void visit(MethodCallExpr c, Void arg) {
                        for (Expression argument : c.getArguments()) {
                            String key = argument.toString();
                            appendDependence(key, ACCESS_KEY, m);
                        }
                    }
                }.visit(m, null);
            }
        }.visit(cu, null);
    }

    /**
     * Only add the dependence if the variable is already identified and has the correct class
     *
     * @param variableKey the name of the variable
     * @param keyType     the type(assign or access) of the method to the variable
     * @param m           method declaration object
     */
    public void appendDependence(String variableKey, String keyType, MethodDeclaration m) {
        String className = m.resolve().getClassName();
        String methodName = String.join(".", m.resolve().getClassName(), m.getNameAsString());
        if (dependence.has(className)) {
            JSONArray dependency = dependence.getJSONArray(className);
            for (int i = 0; i < dependency.length(); i++) {
                if (dependency.getJSONObject(i).get(FIELD_KEY).equals(variableKey)) {
                    JSONObject tmp = dependency.getJSONObject(i);
                    tmp.append(keyType, methodName);
                    dependency.put(i, tmp);
                }
            }
            dependence.remove(className);
            dependence.put(className, dependency);
        }
    }

    public void appendDependence(String variableKey, String keyType, String variableClass, MethodDeclaration m) {
        if (dependence.has(variableClass)) {
            JSONArray dependency = dependence.getJSONArray(variableClass);
            for (int i = 0; i < dependency.length(); i++) {
                if (dependency.getJSONObject(i).get(FIELD_KEY).equals(variableKey)) {
                    JSONObject tmp = dependency.getJSONObject(i);
                    tmp.append(keyType, String.join(".", m.resolve().getClassName(), m.getNameAsString()));
                    dependency.put(i, tmp);
                }
            }
            dependence.remove(variableClass);
            dependence.put(variableClass, dependency);
        }
    }

    public boolean dependenceHasValue(String className, String value) {
        if (dependence.has(className)) {
            JSONArray dependency = dependence.getJSONArray(className);
            for (int i = 0; i < dependency.length(); i++) {
                if (dependency.getJSONObject(i).get(FIELD_KEY).equals(value)) {
                    return true;
                }
            }
        }
        return false;
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
            JSONArray tmp = dependence.getJSONArray(key);
            for (int i = 0; i < tmp.length(); i++) {
                JSONObject tmpObj = tmp.getJSONObject(i);
                if (tmpObj.has(ASSIGN_KEY) && tmpObj.has(ACCESS_KEY)) {
                    JSONArray assignArray = tmpObj.getJSONArray(ASSIGN_KEY);
                    JSONArray accessArray = tmpObj.getJSONArray(ACCESS_KEY);
                    for (Object methodAssign : assignArray) {
                        int indexAssign = trace.indexOf(methodAssign.toString());
                        for (Object methodAccess : accessArray) {
                            int indexAccess = trace.indexOf(methodAccess.toString());
                            if (indexAssign > -1 && indexAccess > -1 && indexAccess > indexAssign) {
                                graph.addNodeAndEdge(methodAssign.toString(), methodAccess.toString(), Digraph.STYLE_DATA);
                            }
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
