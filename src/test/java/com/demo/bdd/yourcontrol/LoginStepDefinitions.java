package com.demo.bdd.yourcontrol;

import com.demo.core.logger.DefaultLogger;
import com.demo.pages.Sites;
import io.cucumber.java.en.When;

public class LoginStepDefinitions extends DefaultLogger {

    private final String siteName = "[yourcontrol] Login -> ";

    @When(siteName + "Open login page")
    public void openLoginPageStep() {
        Sites.yourControlPages().loginPage().openLoginPage();
    }

    @When(siteName + "Type email {string}")
    public void typeEmail(String email){
        Sites.yourControlPages().loginPage().typeEmail(email);
    }

    @When(siteName + "Type password {string}")
    public void typePassword(String password){
        Sites.yourControlPages().loginPage().typePassword(password);
    }

    @When(siteName + "Click login button")
    public void clickLoginButton(){
        Sites.yourControlPages().loginPage().clickLogInButton();
    }
}
