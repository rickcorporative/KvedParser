package com.demo.pages.yourcontrol;

import com.demo.core.base.PageTools;
import com.demo.utils.Constants;
import com.demo.utils.PlaywrightTools;
import io.qameta.allure.Step;

public class LoginPage extends PageTools {
    private String uniField = "//input[@id='loginform-%s']";
    private String logInButton = "//button[@name='login-button']";

    @Step("Open login page")
    public void openLoginPage(){
        PlaywrightTools.openUrl(Constants.LOGIN_URL);
    }

    @Step("Type email")
    public void typeEmail(String value){
        type(value, uniField, "login");
    }

    @Step("Type password")
    public void typePassword(String value){
        type(value, uniField, "password");
    }

    @Step("Click sign in button")
    public void clickLogInButton(){
        click(logInButton);
    }
}
