package com.demo.actions;

import com.demo.core.config.PlaywrightConfig;

public class Actions {
    /**
     * Page actions
     */
    private static MainActions mainActions;

    /**
     * This function returns an instance of `LoginActions`
     */


    /**
     * This function returns an instance of `MainActions`
     */
    public static MainActions mainActions() {
        if (mainActions == null) {
            mainActions = new MainActions();
        }
        return mainActions;
    }

    /**
     * These functions return an instance of `APIActions`
     */
    public synchronized static APIActions apiActions(boolean isContextFromBrowser) {
        return new APIActions(
                isContextFromBrowser
                        ? PlaywrightConfig.getPage().request()
                        : PlaywrightConfig.getAPIRequestContextNew().newContext()
        );
    }

    public static APIActions apiActions() {
        return apiActions(true);
    }
}