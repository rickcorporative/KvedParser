package com.demo.bdd.yourcontrol;

import com.demo.core.logger.DefaultLogger;
import com.demo.pages.Sites;
import io.cucumber.java.en.When;

public class CompanyStepDefinitions extends DefaultLogger {

    private final String siteName = "[yourcontrol] Company -> ";

    @When(siteName + "Print phone")
    public void printPhone() {
        Sites.yourControlPages().companyPage().getCompanyPhone();
    }
}
