package com.demo.actions;

import com.demo.utils.Constants;
import com.demo.utils.PlaywrightTools;

public class LoginActions {

    public void login(){
        PlaywrightTools.openUrl(Constants.LOGIN_URL);
    }
}
