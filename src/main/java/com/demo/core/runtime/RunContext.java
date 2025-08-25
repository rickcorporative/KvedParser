package com.demo.core.runtime;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

public final class RunContext {
    private RunContext() {}

    private static final ThreadLocal<Page> TL_PAGE = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> TL_CTX = new ThreadLocal<>();

    public static void set(Page page, BrowserContext ctx) {
        TL_PAGE.set(page);
        TL_CTX.set(ctx);
    }
    public static Page getPage() { return TL_PAGE.get(); }
    public static BrowserContext getContext() { return TL_CTX.get(); }

    public static void clear() {
        TL_PAGE.remove();
        TL_CTX.remove();
    }
}
