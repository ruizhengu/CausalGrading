package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
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

    public static List<String[]> readAllData(String file) {
        try {
            FileReader fileReader = new FileReader(file);
            CSVReader csvReader = new CSVReader(fileReader);
            return csvReader.readAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
