package org.example;

import com.github.javaparser.utils.CodeGenerationUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static Set<File> files = new HashSet<>();

    /**
     * Get all the files except the test files in the project.
     *
     * @param dir The path of main/java directory
     * @return All the files in the directory
     */
    public static Set<File> listFiles(File dir) {
        for (File entry : dir.listFiles()) {
            if (entry.isDirectory()) {
                listFiles(entry);
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
    public static String getSimpleClassName(String fullyQualifiedName) {
        Pattern p = Pattern.compile(".*\\.(.*)");
        Matcher m = p.matcher(fullyQualifiedName);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
