package org.example;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Coverage {

    public static String METHOD_COVERAGE_DIRECTORY = "MethodCoverage";

    public void getTrace() {
        for (String methods : listTestMethods()) {
            System.out.println(methods);
        }
    }

    public List<String> listTestMethods() {
        String currentClass = this.getClass().getSimpleName();
        return Stream.of(Objects.requireNonNull(new File(METHOD_COVERAGE_DIRECTORY).listFiles())).map(File::getName).filter(name -> !name.contains(currentClass)).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Coverage coverage = new Coverage();
        coverage.getTrace();
    }
}
