package com.demo.pages;

import com.demo.core.logger.DefaultLogger;
import com.demo.pages.testSite.TestPage;
import com.demo.pages.yourcontrol.YourControlPages;

public class Sites extends DefaultLogger {
    /**
     * Sites
     */
    private static YourControlPages yourControlPages;

    /**
     * This function return an instance of `YourControlPages`
     */
    public static YourControlPages yourControlPages(){
        if(yourControlPages == null) {
            yourControlPages = new YourControlPages();
        }
        return yourControlPages;
    }
}
