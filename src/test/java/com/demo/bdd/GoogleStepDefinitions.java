package com.demo.bdd;

import com.demo.core.logger.DefaultLogger;
import com.demo.core.config.PlaywrightConfig;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class GoogleStepDefinitions extends DefaultLogger {

    @Given("Check precondition status")
    public void checkPreconditionStatus() {
        Assert.assertTrue(true);
    }

    @When("Open google")
    public void openGoogle() {
        PlaywrightConfig.getPage().navigate("https://www.google.com/");
        Assert.assertTrue("Should be true", false);
    }
}
