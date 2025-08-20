package com.demo.core.config;

import com.microsoft.playwright.Playwright;

public final class PlaywrightHolder {

    private PlaywrightHolder() {
    }

    private static final ThreadLocal<Playwright> THREAD_LOCAL =
            new ThreadLocal<>();

    public synchronized static Playwright get() {
        Playwright pw = THREAD_LOCAL.get();
        if (pw == null) {
            pw = Playwright.create();
            THREAD_LOCAL.set(pw);
        }
        return pw;
    }

    public static void shutdown() {
        Playwright pw = THREAD_LOCAL.get();
        if (pw != null) {
            pw.close();
            THREAD_LOCAL.remove();
        }
    }
}