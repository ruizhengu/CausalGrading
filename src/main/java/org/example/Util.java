package org.example;

import com.github.javaparser.utils.CodeGenerationUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
    public static Path gradleModuleRoot(Class<?> c) {
        String buildFileName = "build.gradle";


        Path other = Paths.get(".");
        Path normalize = CodeGenerationUtils
                .classLoaderRoot(c)
                .resolve(other)
                .normalize();

        // If it's not a directory, or if it is a directory and the build file isn't present, go up a level.
        while (!normalize.toFile().isDirectory() || !normalize.resolve(buildFileName).toFile().exists()) {
            System.out.println("source root not found at + " + normalize + ", trying next directory up");
            other = other.resolve("..");
            normalize = CodeGenerationUtils
                    .classLoaderRoot(c)
                    .resolve(other)
                    .normalize();
        }

        return normalize;
    }
}
