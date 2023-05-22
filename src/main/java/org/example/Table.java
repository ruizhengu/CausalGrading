package org.example;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Table {
    public static String METHOD_COVERAGE_TABLE = "mct.csv";

    public static void writeMethodCoverage(List<String[]> data) {
        File methodCoverageFile = new File(METHOD_COVERAGE_TABLE);
        try {
            FileWriter output = new FileWriter(methodCoverageFile);
            CSVWriter writer = new CSVWriter(output);
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
