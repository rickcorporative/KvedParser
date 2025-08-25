package com.demo.utils;

import com.demo.core.runtime.RunContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlaywrightTools {
    protected static final Logger LOG = LoggerFactory.getLogger(PlaywrightTools.class);

    private static Page getPage() {
        Page p = RunContext.getPage();
        if (p == null) {
            throw new IllegalStateException("❌ Page is null. Убедитесь, что Hooks.beforeScenario создал страницу и вызвал RunContext.set(page, context).");
        }
        return p;
    }

    public static List<Page> getTabsList() {
        return getPage().context().pages();
    }

    public static int getTabsCount() {
        return getTabsList().size();
    }

    public static void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Step("Closing current tab...")
    public static void closeCurrentTab() {
        LOG.info("Closing current tab...");
        getPage().close();
    }

    @Step("Opening url - '{url}'")
    public static void openUrl(String url) {
        LOG.info("Opening url '{}'", url);
        getPage().navigate(url);
    }

    @Step("Refreshing...")
    public static void refresh() {
        LOG.info("Refreshing...");
        getPage().reload();
    }

    @Step("Clearing cookies...")
    public static void clearCookies() {
        LOG.info("Clearing cookies...");
        getPage().context().clearCookies();
    }

    @Step("Switching to frame by name...")
    public static void switchToFrame(String frameName) {
        LOG.info("Switching to frame '{}'", frameName);
        Frame frame = getPage().frame(frameName);
        if (frame == null) {
            throw new RuntimeException("Frame with name '" + frameName + "' not found");
        }
    }

    @Step("Switching to default content...")
    public static void switchToDefaultContent() {
        LOG.info("Switching to default content...");
    }

    @Step("Switching to first tab...")
    public static void switchToFirstTab() {
        LOG.info("Switching to first tab...");
        List<Page> tabs = getTabsList();
        if (!tabs.isEmpty()) {
            RunContext.set(tabs.get(0), RunContext.getContext());
        }
    }

    @Step("Switching to last tab...")
    public static void switchToLastTab() {
        LOG.info("Switching to last tab...");
        List<Page> tabs = getTabsList();
        Page lastPage = tabs.get(tabs.size() - 1);
        RunContext.set(lastPage, RunContext.getContext());
    }

    @Step("Open url in new window")
    public static void openUrlInNewWindow(String url) {
        LOG.info("Open url in new window");
        Page newPage = getPage().context().newPage();
        newPage.navigate(url);
        RunContext.set(newPage, RunContext.getContext());
    }

    public static void closeAllTabsExceptCurrent() {
        Page current = getPage();
        for (Page tab : getTabsList()) {
            if (!tab.equals(current)) {
                tab.close();
            }
        }
    }

    @Step("Get current url")
    public static String getCurrentUrl() {
        LOG.info("Get current url");
        return getPage().url();
    }

    public static void setViewportSize(int width, int height) {
        getPage().setViewportSize(width, height);
    }
}
