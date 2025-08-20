package com.demo.core.base;

import com.demo.core.logger.DefaultLogger;
import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


public class BaseTest extends DefaultLogger {

    protected Page page;

    @BeforeMethod(alwaysRun = true, description = "Opening web browser...")
    public void setUp() {
        logInfo("Started test...");
    }

    @AfterMethod(alwaysRun = true, description = "Closing web browser...")
    public void tearDown(ITestResult result) {
        logInfo("Ended test...");
    }
}
