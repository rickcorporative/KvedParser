package com.demo.core.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.stream.Stream;

public class Retry implements IRetryAnalyzer {
    private static int maxTry = 1;
    private int count = 0;

    @Override
    public boolean retry(ITestResult iTestResult) {
        boolean doNotRetry = Stream.of(iTestResult.getMethod().getRealClass().getMethods())
                .anyMatch(method -> method.isAnnotationPresent(DoNotRetry.class));

        if (!iTestResult.isSuccess() && !doNotRetry) {
            if (count < maxTry) {
                count++;
                iTestResult.setStatus(ITestResult.FAILURE);
                return true;
            } else {
                iTestResult.setStatus(ITestResult.FAILURE);
            }
        } else {
            iTestResult.setStatus(ITestResult.SUCCESS);
        }
        return false;
    }
}
