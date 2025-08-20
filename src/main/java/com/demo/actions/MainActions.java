package com.demo.actions;

import com.demo.core.config.PlaywrightConfig;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class MainActions {
    private final Page page;
    private final BrowserContext context;

    public MainActions() {
        this.page = PlaywrightConfig.getPage();
        this.context = this.page.context();
    }

    public MainActions(Page page) {
        this.page = page;
        this.context = page.context();
    }

    public void openNewTab() {
        context.newPage();
    }

    public void openLinkFromClipboard() throws IOException, UnsupportedFlavorException {
        String clipboardValue = Toolkit.getDefaultToolkit()
                .getSystemClipboard().getData(DataFlavor.stringFlavor).toString();

        page.navigate(clipboardValue);
    }

    public String getCurrentUrl() {
        return page.url();
    }

}