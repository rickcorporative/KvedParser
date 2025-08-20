package com.demo.actions;

import com.demo.core.logger.DefaultLogger;
import com.demo.utils.Constants;
import com.demo.utils.ParseJSON;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIActions extends DefaultLogger {
    private final APIRequestContext context;

    public APIActions(APIRequestContext context) {
        this.context = context;
    }


    /*
    Private methods
     */

    private List<String> getListOfPartsEmailWithPlus(String email) { //Example of email test+451423@gmail.com
        int atIndex = email.indexOf('@');
        int plusIndex = email.indexOf('+');
        if (atIndex == 0 || plusIndex == 0) return null; // not a valid email

        return List.of(email.substring(0, plusIndex), email.substring(plusIndex + 1, atIndex), email.substring(atIndex + 1));
    }

    private List<String> getListOfPartsEmail(String email) { //Example of email test@gmail.com
        int atIndex = email.indexOf('@');
        if (atIndex == 0) return null; // not a valid email

        return List.of(email.substring(0, atIndex), email.substring(atIndex + 1));
    }


    private synchronized APIResponse getResponse(String typeResponse, String urlForResponse, RequestOptions requestOptions) {
        APIResponse response = switch (typeResponse.toUpperCase()) {
            case "GET" -> context.get(urlForResponse, requestOptions);
            case "DELETE" -> context.delete(urlForResponse, requestOptions);
            case "POST" -> context.post(urlForResponse, requestOptions);
            default -> throw new IllegalArgumentException("Unknown type response: " + typeResponse);
        };

        logInfo("Response [" + typeResponse + "] code: " + response.status());
        return response;
    }

    private String getUrlForResponse(String url) {
        String urlForResponse = Constants.BASE_URL + url;
        logInfo("URL for response: " + urlForResponse);
        return urlForResponse;
    }
}
