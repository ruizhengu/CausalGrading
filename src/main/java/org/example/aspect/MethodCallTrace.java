package org.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import java.io.FileWriter;
import java.io.IOException;

@Aspect
public class MethodCallTrace {
    public FileWriter writer;

    public MethodCallTrace() throws IOException {
        writer = new FileWriter("log.txt");
    }

    @After("execution(* uk.ac.sheffield.com1003.cafe.*.*(..))")
    public void logMethodExecution(JoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        writer.write("Method executed: " + methodName + "\n");
        writer.flush();
    }

    @AfterThrowing(pointcut = "execution(* uk.ac.sheffield.com1003.cafe.*.*(..))", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Throwable ex) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        writer.write("Exception thrown in method: " + methodName + " - " + ex.getMessage() + "\n");
        writer.flush();
    }

//    @After("execution(* uk.ac.sheffield.com1003.cafe.*.*(..))")
//    public void closeLogFile() throws Throwable {
//        writer.close();
//    }

}
