package com.demo.core.config;

import com.demo.utils.Constants;
import com.demo.utils.PlaywrightTools;
import com.microsoft.playwright.*;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.options.RecordVideoSize;
import com.microsoft.playwright.options.ViewportSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PlaywrightConfig {
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<APIRequest> apiRequestThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> scenarioNameThreadLocal = new ThreadLocal<>();

    private static final double timeoutTime = Constants.TIMEOUT_BEFORE_FAIL * 1000;


    public static void createBrowserConfig() {
        getBrowser();
        getContext();
        getPage();
    }

    public static Page getPage() {
        if (pageThreadLocal.get() == null) {
            pageThreadLocal.set(getContext().newPage());
            pageThreadLocal.get().setDefaultTimeout(timeoutTime);
        }
        return pageThreadLocal.get();
    }

    public static void setPage(Page newPage) {
        pageThreadLocal.set(newPage);
    }

    public static Browser getBrowser() {
        if (browserThreadLocal.get() == null) {
            boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));
            String browserType = System.getProperty("playwright.browser", "chrome");

            LaunchOptions options = new LaunchOptions()
                    .setHeadless(isHeadless)
                    .setArgs(List.of("--disable-notifications", "--start-maximized", "--window-size=" + Constants.SCREEN_WIDTH + "," + Constants.SCREEN_HEIGHT))
                    .setTimeout(timeoutTime);

            browserThreadLocal.set(switch (browserType.toLowerCase()) {
                case "chrome" -> {
                    options.setChannel("chrome");
                    yield PlaywrightHolder.get().chromium().launch(options);
                }
                case "firefox" -> PlaywrightHolder.get().firefox().launch(options);
                default -> throw new IllegalArgumentException("Unsupported browser: " + browserType);
            });
        }
        return browserThreadLocal.get();
    }

    public static BrowserContext getContext() {
        if (contextThreadLocal.get() == null) {
            String scenarioName = getScenarioName();
            Path videoDir = Paths.get("target/videos", scenarioName != null ? scenarioName : Thread.currentThread().getName());

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(new ViewportSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT))
                    .setRecordVideoSize(new RecordVideoSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT))
                    .setAcceptDownloads(true)
                    .setIgnoreHTTPSErrors(true)
                    .setRecordVideoDir(videoDir);

            contextThreadLocal.set(getBrowser().newContext(contextOptions));
        }
        return contextThreadLocal.get();
    }

    public synchronized static APIRequest getAPIRequestContextNew() {
        Playwright playwright = PlaywrightHolder.get();
        if (playwright == null) {
            throw new IllegalStateException("Playwright not initialized. Call Playwright.create() first.");
        }
        if (apiRequestThreadLocal.get() == null)
            apiRequestThreadLocal.set(playwright.request());
        return apiRequestThreadLocal.get();
    }

    public synchronized static void closeBrowser() {
        boolean holdBrowserOpen = Boolean.parseBoolean(System.getProperty("holdBrowserOpen", "false")); //For debugging only
        if (holdBrowserOpen) {
            PlaywrightTools.sleep(Constants.BIG_TIMEOUT * 25); // Stop for 25*25 = 10~ minutes
        }

        BrowserContext context = contextThreadLocal.get();
        if (context != null) {
            context.close();
            contextThreadLocal.remove();

            Browser browser = browserThreadLocal.get();
            if (browser != null) {
                browser.close();
                browserThreadLocal.remove();
            }

            apiRequestThreadLocal.remove();
            pageThreadLocal.remove();
            scenarioNameThreadLocal.remove();
        }
    }

    public static void setScenarioName(String name) {
        scenarioNameThreadLocal.set(name);
    }

    public static String getScenarioName() {
        return scenarioNameThreadLocal.get();
    }
}
