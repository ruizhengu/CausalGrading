package org.example;

import com.github.javaparser.ast.expr.MethodCallExpr;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static Set<File> files = new HashSet<>();
    public static String PACKAGE_NAME = "uk.ac.sheffield.com1003.cafe";

    /**
     * Get the project path by the current OS
     *
     * @return the abs path of /src/main/java
     */
    public static String getOSPath() {
        String macPath = "/Users/ray/Project/PhD/GTA/com1003_cafe/src/main/java";
        String linuxPath = "/home/ruizhen/Projects/Experiment/com1003_cafe/src/main/java";
        if (System.getProperty("os.name").startsWith("Mac")) {
            return macPath;
        } else {
            return linuxPath;
        }
    }

    /**
     * Get all the files except the test files in the project.
     *
     * @param dir The path of main/java directory
     * @return All the files in the directory
     */
    public static Set<File> getFiles(File dir) {
        for (File entry : Objects.requireNonNull(dir.listFiles())) {
            if (entry.isDirectory()) {
                getFiles(entry);
            } else {
                files.add(entry);
            }
        }
        return files;
    }

    /**
     * Get the .java file the method belongs by the method call expression
     *
     * @param expr Method call expression object
     * @return The .java file the method belongs
     */
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


    /**
     * Get the last segment from a fully qualified name
     *
     * @param fullyQualifiedName e.g. uk.ac.sheffield.com1003.cafe.ingredients.Water
     * @return e.g. Water
     */
    public static String getLastSegment(String fullyQualifiedName) {
        String[] splits = fullyQualifiedName.split("\\.");
        return splits[splits.length - 1];
    }

    /**
     * Get the last 'length' number of segments from a fully qualified name
     *
     * @param fullyQualifiedName e.g. uk.ac.sheffield.com1003.cafe.ingredients.Water
     * @param length             the length of segments
     * @return e.g. ingredients.Water if index == 2
     */
    public static String getLastSegment(String fullyQualifiedName, int length) {
        String[] splits = fullyQualifiedName.split("\\.");
        StringBuilder output = new StringBuilder(splits[splits.length - length]);
        for (int i = 1; i < length; i++) {
            output.append(".").append(splits[splits.length - length + i]);
        }
        return output.toString();
    }

    /**
     * Get the 'length' number of segments start from 'index'
     *
     * @param fullyQualifiedName e.g. e.g. uk.ac.sheffield.com1003.cafe.ingredients.Water.Type
     * @param index              the index of the segments (from the end of the string)
     * @param length             the length of the segments
     * @return e.g. Water if index == 2, length == 1
     */
    public static String getLastSegment(String fullyQualifiedName, int index, int length) {
        if (length > index) {
            throw new IndexOutOfBoundsException();
        }
        String[] splits = fullyQualifiedName.split("\\.");
        StringBuilder output = new StringBuilder(splits[splits.length - index]);
        for (int i = 1; i < length; i++) {
            output.append(".").append(splits[splits.length - length + i]);
        }
        return output.toString();
    }
}
