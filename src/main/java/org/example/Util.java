package org.example;

import com.github.javaparser.utils.CodeGenerationUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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
}
