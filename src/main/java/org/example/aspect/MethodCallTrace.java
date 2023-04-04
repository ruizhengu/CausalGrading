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

    @Pointcut("execution(* uk.ac.sheffield.com1003.cafe.*.*(..)) || execution(* uk.ac.sheffield.com1003.cafe.*.*.*(..))")
    public void recordExecution() {
    }

    @Before("recordExecution()")
    public void logMethodExecution(JoinPoint joinPoint) throws Throwable {
        String methodName = String.join(".", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        writer.write(methodName + "\n");
        writer.flush();
    }
}
