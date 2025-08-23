package com.demo.pages.yourcontrol;

import com.demo.core.logger.DefaultLogger;

public class YourControlPages extends DefaultLogger {
    /**
     * Pages
     */
    private static HomePage homePage;
    private static CompanyPage companyPage;
    private static LoginPage loginPage;

    /**
     * This function return an instance of `HomePage`
     */
    public static HomePage homePage(){
        if(homePage == null) {
            homePage = new HomePage();
        }
        return homePage;
    }

    /**
     * This function return an instance of `CompanyPage`
     */
    public static CompanyPage companyPage(){
        if(companyPage == null) {
            companyPage = new CompanyPage();
        }
        return companyPage;
    }

    /**
     * This function return an instance of `LoginPage`
     */
    public static LoginPage loginPage(){
        if(loginPage == null) {
            loginPage = new LoginPage();
        }
        return loginPage;
    }
}
