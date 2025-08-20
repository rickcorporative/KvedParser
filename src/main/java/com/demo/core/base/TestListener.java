package com.demo.core.base;

import com.demo.core.allure.AllureTools;
import com.demo.core.config.PlaywrightConfig;
import com.demo.utils.PlaywrightTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static String getTestMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getConstructorOrMethod().getName();
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
        LOG.info("STARTED test: {}", getTestMethodName(iTestResult));
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        LOG.info("PASSED test: {}", getTestMethodName(iTestResult));
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        LOG.error("FAILED test: {}", getTestMethodName(iTestResult));
        AllureTools.attachScreenshot(PlaywrightConfig.getPage());
        AllureTools.attachLogFile();
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        LOG.error("SKIPPED test: {}", getTestMethodName(iTestResult));
        AllureTools.attachScreenshot(PlaywrightConfig.getPage());
        AllureTools.attachLogFile();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        LOG.info("Test failed but it is in defined success ratio: {}", getTestMethodName(iTestResult));
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }
}
