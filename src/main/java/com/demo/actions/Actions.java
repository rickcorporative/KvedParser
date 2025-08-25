package com.demo.actions;

import com.demo.core.runtime.RunContext;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

public class Actions {
    /**
     * Page actions
     */
    private static MainActions mainActions;

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
     *
     * @param isContextFromBrowser true  — использовать APIRequestContext текущего BrowserContext (разделяет куки/заголовки с UI)
     *                             false — создать новый BrowserContext и взять у него request() (чистая сессия)
     */
    public static synchronized APIActions apiActions(boolean isContextFromBrowser) {
        BrowserContext baseCtx = RunContext.getContext();
        if (baseCtx == null) {
            throw new IllegalStateException("RunContext.getContext() is null. Убедитесь, что Hooks.beforeScenario создаёт BrowserContext и вызывает RunContext.set(page, context).");
        }

        APIRequestContext apiCtx;
        if (isContextFromBrowser) {
            // общий контекст с UI: наследует cookies/storage того же BrowserContext
            apiCtx = baseCtx.request();
            return new APIActions(apiCtx);
        } else {
            // чистый контекст: новый BrowserContext в том же Browser
            Browser browser = baseCtx.browser();
            BrowserContext freshCtx = browser.newContext();
            apiCtx = freshCtx.request();
            // ВАЖНО: где-то должен быть lifecycle-менеджмент freshCtx (закрыть после использования).
            // Если ваш APIActions управляет закрытием APIRequestContext/Context — отлично.
            // Иначе добавьте явное закрытие там, где заканчиваете API-сценарий.
            return new APIActions(apiCtx);
        }
    }

    public static APIActions apiActions() {
        return apiActions(true);
    }
}
