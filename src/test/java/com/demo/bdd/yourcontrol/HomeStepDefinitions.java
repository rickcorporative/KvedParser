package com.demo.bdd.yourcontrol;

import com.demo.core.logger.DefaultLogger;
import com.demo.pages.Sites;
import io.cucumber.java.en.When;

public class HomeStepDefinitions extends DefaultLogger {

    private final String siteName = "[yourcontrol] Home -> ";

    @When(siteName + "Open home page")
    public void openHomePageStep() {
        Sites.yourControlPages().homePage().openHomePage();
    }

    @When(siteName + "Click search button")
    public void clickSearchButtonStep() {
        Sites.yourControlPages().homePage().clickSearchButton();
    }

    @When(siteName + "Type {string} into input")
    public void typeIntoInput(String value) {
        Sites.yourControlPages().homePage().typeIntoInput(value);
    }

    @When(siteName + "Parse announcements")
    public void parseAnnounce(){
        Sites.yourControlPages().homePage().parse();
    }
}
