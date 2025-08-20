package com.demo.bdd;

import com.demo.core.logger.DefaultLogger;
import com.demo.utils.Constants;
import com.demo.utils.PlaywrightTools;
import io.cucumber.java.en.When;

import java.util.Map;

public class DefaultStepDefinitions extends DefaultLogger {

    private Map<String, Integer> timeoutMap = Map.of(
            "NANO", Constants.NANO_TIMEOUT,
            "MICRO", Constants.MICRO_TIMEOUT,
            "MINI", Constants.MINI_TIMEOUT,
            "SMALL", Constants.SMALL_TIMEOUT,
            "BIG", Constants.BIG_TIMEOUT
    );

    @When("Open duplicate of current tab in other tab and switch on it")
    public void openNewTabDuplicateOfCurrent() {
        PlaywrightTools.openUrlInNewWindow(PlaywrightTools.getCurrentUrl());
        PlaywrightTools.switchToLastTab();
    }

    @When("Close current tab")
    public void closeCurrentTab() {
        PlaywrightTools.closeCurrentTab();
    }

    @When("Open new tab")
    public void openNewTab() {
        PlaywrightTools.openUrlInNewWindow(Constants.BASE_URL);
    }

    @When("Refresh page after {int} seconds")
    public void refreshPageAfterValueSeconds(int seconds) {
        PlaywrightTools.sleep(seconds);
        PlaywrightTools.refresh();
    }

    @When("Timeout {int} seconds")
    public void timeoutSeconds(int seconds) {
        PlaywrightTools.sleep(seconds);
    }

    @When("Timeout {string}")
    public void timeoutSeconds(String valueOfSeconds) {
        Integer timeout = timeoutMap.get(valueOfSeconds.toUpperCase());
        if (timeout != null) {
            PlaywrightTools.sleep(timeout);
        } else {
            logError("Unknown timeout level: " + valueOfSeconds + ". Please use one of: " + timeoutMap.keySet() + " values." + " Default value is " + Constants.SMALL_TIMEOUT + " seconds.");
            logInfo("Using default timeout of " + Constants.SMALL_TIMEOUT + " seconds.");
            PlaywrightTools.sleep(Constants.SMALL_TIMEOUT);
        }
    }
}
