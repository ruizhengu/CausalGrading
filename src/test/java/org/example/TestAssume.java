package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class TestAssume {

    @Test
    public void testA() {
        boolean testACondition = false;
        Assertions.assertTrue(testACondition);
    }

    @Test
    public void testB() {
//        boolean testACondition = false;
//        Assumptions.assumeTrue(!testACondition);

        boolean testBCondition = true;
        Assertions.assertTrue(testBCondition);
    }
}

