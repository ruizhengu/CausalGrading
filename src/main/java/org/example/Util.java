package org.example;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.utils.CodeGenerationUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    //    private static String PACKAGE_NAME = ""
    public static Set<File> files = new HashSet<>();

    /**
     * Get all the files except the test files in the project.
     *
     * @param dir The path of main/java directory
     * @return All the files in the directory
     */
    public static Set<File> getFiles(File dir) {
        for (File entry : dir.listFiles()) {
            if (entry.isDirectory()) {
                getFiles(entry);
            } else {
                files.add(entry);
            }
        }
        return files;
    }

    /**
     * Get the simple class name from a fully qualified class name
     *
     * @param fullyQualifiedName e.g. uk.ac.sheffield.com1003.cafe.ingredients.Coffee
     * @return e.g. Coffee
     */
    public static String getSimplifiedName(String fullyQualifiedName) {
        Pattern p = Pattern.compile(".*\\.(.*)");
        Matcher m = p.matcher(fullyQualifiedName);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }


    /**
     * Get the fully qualified class name from a method call expression
     *
     * @param expr A MethodCallExpr object
     * @return e.g. uk.ac.sheffield.com1003.cafe.Cafe.addRecipe
     */
    public static String getFullyQualifiedClassName(MethodCallExpr expr) {
//        System.out.println(expr.resolve().getPackageName());
//        System.out.println(expr.getScope().get().calculateResolvedType().describe());
        return expr.getScope().get().calculateResolvedType().describe() + "." + expr.getName();
    }

    public static boolean ifPackageClass(MethodCallExpr expr) {
        String qualifiedName = expr.resolve().getQualifiedName();
        String packageName = expr.resolve().getPackageName();
        System.out.println("qualified name " + qualifiedName);
        System.out.println("package name " + packageName);
        if (qualifiedName.contains(packageName)) {
            return true;
        } else {
            return false;
        }
    }

//    public static File getClassByMethod(MethodCallExpr expr) {
//        String fullyQualifiedClassName = getFullyQualifiedClassName(expr);
//        return getFileOfMethod(fullyQualifiedClassName);
//    }

    public static File getFileOfMethod(MethodCallExpr expr) {
        String classOfMethod = null;
        Pattern p = Pattern.compile("(.*)\\..*");
        Matcher m = p.matcher(expr.resolve().getQualifiedName());
        if (m.find()) {
            classOfMethod = m.group(1);
        }
        for (File file : files) {
            assert classOfMethod != null;
            if (file.toString().contains(classOfMethod.replace(".", "/"))) {
                return file;
            }
        }
        return null;
    }
}
