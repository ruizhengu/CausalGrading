package org.example;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Coverage {
    public static void main(String[] args) {
        ExecutionDataReader executionDataReader;
        {
            try {
                executionDataReader = new ExecutionDataReader(new FileInputStream("/home/ruizhen/Projects/Experiment/com1003-problem-sheet-guruizhen/build/jacoco/test.exec"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            ExecutionDataStore executionDataStore = new ExecutionDataStore();
            SessionInfoStore sessionInfoStore = new SessionInfoStore();
            executionDataReader.setExecutionDataVisitor(executionDataStore);
            executionDataReader.setSessionInfoVisitor(sessionInfoStore);
            try {
                executionDataReader.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            CoverageBuilder coverageBuilder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);


//            try {
//                analyzer.analyzeAll(new File("/home/ruizhen/Projects/Experiment/com1003-problem-sheet-guruizhen/build/classes"));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            IClassCoverage classCoverage = coverageBuilder.getClasses().stream().findFirst().orElse(null);
//            if (classCoverage != null) {
//                List<String> coveredMethods = classCoverage.getMethods().stream().map(ICoverageNode::getName).collect(Collectors.toList());
//
//                for (String method : coveredMethods) {
//                    System.out.println(method);
//                }
//            }
//            for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
//                List<String> coveredMethods = classCoverage.getMethods().stream().map(ICoverageNode::getName).collect(Collectors.toList());
//                System.out.println(classCoverage + " " + coveredMethods);
//            }
        }
    }
}
