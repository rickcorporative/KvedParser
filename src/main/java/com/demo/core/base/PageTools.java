package com.demo.core.base;

import com.demo.core.logger.DefaultLogger;
import com.demo.core.runtime.RunContext;
import com.demo.utils.Constants;
import com.demo.utils.LocatorParser;
import com.demo.utils.PlaywrightTools;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

public class PageTools extends DefaultLogger {

    // ---- Больше НЕ храним Page в поле, чтобы не держать ссылку на закрытую страницу ----
    public PageTools() { }
    public PageTools(Page ignored) { } // оставлен для совместимости со старыми вызовами

    public static Page getPage() {
        Page p = RunContext.getPage();
        if (p == null || p.isClosed()) {
            throw new IllegalStateException("❌ Page is null or closed. Убедись, что Hooks.beforeScenario вызвал RunContext.set(page, context).");
        }
        return p;
    }

    private static String getPreviousMethodNameAsText() {
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        String replacedMethodName = methodName.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
        return replacedMethodName.substring(0, 1).toUpperCase() + replacedMethodName.substring(1).toLowerCase();
    }

    private Locator byLocator(String by, Object... args) {
        return LocatorParser.parseLocator(getPage(), by, args);
    }

    /**
     * Should be
     */
    protected void shouldMatchText(String pattern, String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            String actualText = locator.innerText();
            if (!actualText.matches(pattern)) {
                throw new AssertionError("Text does not match pattern.\nExpected regex: " + pattern + "\nActual text: " + actualText);
            }
        });
    }

    protected void shouldNotBeEmpty(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            String actualText = locator.innerText().trim();
            if (actualText.isEmpty()) {
                throw new AssertionError("Element text is empty, but should not be.");
            }
        });
    }

    protected void shouldNotHaveClass(String className, String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            if (locator.getAttribute("class") != null && locator.getAttribute("class").contains(className)) {
                throw new AssertionError("Element has class '" + className + "' but should not.");
            }
        });
    }

    protected void shouldHaveClass(String className, String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            String classes = locator.getAttribute("class");
            if (classes == null || !classes.contains(className)) {
                throw new AssertionError("Element does not have expected class '" + className + "'. Actual classes: " + classes);
            }
        });
    }

    /**
     * Main Actions
     */
    protected void click(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, Locator::click);
    }

    protected void doubleClick(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, Locator::dblclick);
    }

    protected void jsClick(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.evaluate("el => el.click()");
        });
    }

    @Deprecated
    protected void typeDeprecated(String text, String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.type(text);
        });
    }

    protected void type(String text, String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.fill(text);
        });
    }

    protected void wipeText(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.fill("");
        });
    }

    protected void uploadFile(String filePath, String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.setInputFiles(Paths.get(filePath));
        });
    }

    protected void mouseHover(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, Locator::hover);
    }

    protected void clickEnterButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "Enter", args);
    }

    protected void clickEscapeButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "Escape", args);
    }

    protected void clickTabButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "Tab", args);
    }

    protected void clickBackspaceButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "Backspace", args);
    }

    protected void clickDeleteButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "Delete", args);
    }

    protected void clickArrowLeftButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "ArrowLeft", args);
    }

    protected void clickArrowUpButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "ArrowUp", args);
    }

    protected void clickArrowRightButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "ArrowRight", args);
    }

    protected void clickArrowDownButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "ArrowDown", args);
    }

    protected void clickSpaceButton(String selector, Object... args) {
        clickButtonOnKeyboard(selector, "Space", args);
    }

    private void clickButtonOnKeyboard(String selector, String keyButton, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.press(keyButton);
        });
    }

    protected void selectOption(String selector, String option, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.selectOption(option);
        });
    }

    protected void waitForElementVisibility(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        });
    }

    protected void waitForElementInvisibility(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            try {
                locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
            } catch (PlaywrightException e) {
                if (e.getMessage().contains("Object doesn't exist")) {
                    logInfo("Element already removed: " + selector);
                } else {
                    throw e;
                }
            }
        });
    }

    protected void waitForElementClickable(String selector, Object... args) {
        performOnLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        });
        for (int i = 0; i < 25; i++) {
            if (isElementClickable(selector, args)) {
                return;
            }
            PlaywrightTools.sleep(Constants.NANO_TIMEOUT);
        }
    }

    /**
     * Is condition
     */
    protected boolean isElementVisible(String selector, Object... args) {
        return checkLocatorState(getPreviousMethodNameAsText(), selector, args, Locator::isVisible);
    }

    protected boolean isElementVisibleCheckEverySecond(String locator, int seconds) {
        for (int i = 0; i < seconds; i++) {
            if (isElementVisible(locator)) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    protected boolean isFirstElementVisibleCheckEverySecond(String selector, long seconds, Object... args) {
        Locator first = byLocator(selector, args).first();
        logInfo(getPreviousMethodNameAsText() + ", first element --> " + first);
        try {
            for (int i = 0; i < seconds; i++) {
                if (first.isVisible()) {
                    logInfo("First element is visible after " + i + " seconds");
                    return true;
                }
                PlaywrightTools.sleep(Constants.NANO_TIMEOUT);
            }
            logInfo("First element is not visible after " + seconds + " seconds");
            return false;
        } catch (PlaywrightException e) {
            if (e.getMessage().contains("Object doesn't exist")) {
                logInfo("Element already removed: " + selector);
                return false;
            } else {
                throw e;
            }
        }
    }

    protected boolean isElementClickable(String selector, Object... args) {
        return checkLocatorState(getPreviousMethodNameAsText(), selector, args, Locator::isEnabled);
    }

    protected boolean isElementDisabled(String selector, Object... args) {
        return checkLocatorState(getPreviousMethodNameAsText(), selector, args, Locator::isDisabled);
    }

    protected boolean isElementChecked(String selector, Object... args) {
        return checkLocatorState(getPreviousMethodNameAsText(), selector, args, Locator::isChecked);
    }

    protected boolean isElementVisibleCheckEverySecond(String selector, long seconds, Object... args) {
        Locator parsedLocator = byLocator(selector, args);
        logInfo(getPreviousMethodNameAsText() + ", element --> " + parsedLocator);

        try {
            for (int i = 0; i < seconds; i++) {
                if (parsedLocator.first().isVisible()) {
                    logInfo("Element is visible after " + i + " seconds");
                    return true;
                }
                PlaywrightTools.sleep(Constants.NANO_TIMEOUT);
            }
            logInfo("Element is not visible after " + seconds + " seconds");
            return false;
        } catch (PlaywrightException e) {
            if (e.getMessage().contains("Object doesn't exist")) {
                logInfo("Element already removed: " + selector);
                return false;
            } else {
                throw e;
            }
        }
    }

    /**
     * Getters
     */
    protected String getElementText(String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, Locator::inputValue);
    }

    protected String getElementInnerHTML(String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, Locator::innerHTML);
    }

    protected String getElementInnerText(String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, Locator::innerText);
    }

    protected List<String> getElementsInnerTexts(String locator) {
        return getPage().locator(locator).allInnerTexts();
    }

    protected String getElementTextContent(String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, Locator::textContent);
    }

    protected String getElementAttributeValue(String attr, String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, l -> l.getAttribute(attr));
    }

    protected String getHiddenElementAttributeValue(String attr, String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, l -> {
            if (!l.isHidden()) {
                throw new AssertionError("Element is not hidden");
            }
            return l.getAttribute(attr);
        });
    }

    protected String getDisabledElementAttributeValue(String attr, String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, l -> {
            if (!l.isDisabled()) {
                throw new AssertionError("Element is not disabled");
            }
            return l.getAttribute(attr);
        });
    }

    protected List<String> getElementsText(String selector, Object... args) {
        Locator locator = byLocator(selector, args);
        logInfo(getPreviousMethodNameAsText() + ", elements --> " + locator);
        return locator.allInnerTexts();
    }

    protected List<String> getElementsTextWithWait(int waitTimeout, String selector, Object... args) {
        Locator locator = byLocator(selector, args);
        logInfo(getPreviousMethodNameAsText() + ", elements --> " + locator);
        PlaywrightTools.sleep(waitTimeout);
        return locator.allInnerTexts();
    }

    protected void scrollToElement(String selector, Object... args) {
        Locator locator = byLocator(selector, args);
        logInfo(getPreviousMethodNameAsText() + ", element --> " + locator);
        locator.scrollIntoViewIfNeeded();
    }

    protected void scrollToPlaceElementInCenter(String selector, Object... args) {
        Locator locator = byLocator(selector, args);
        logInfo(getPreviousMethodNameAsText() + ", element --> " + locator);
        getPage().evaluate("el => el.scrollIntoView({block: 'center'})", locator);
    }

    protected ElementHandle getWebElement(String selector, Object... args) {
        Locator locator = byLocator(selector, args);
        return locator.elementHandle();
    }

    /**
     * Downloads
     */
    protected Path downloadFile(String selector, Object... args) {
        try {
            Locator locator = byLocator(selector, args);
            Download download = getPage().waitForDownload(locator::click);
            return download.path();
        } catch (Exception e) {
            logError("Failed to download file using selector '{}'", e, selector);
            return null;
        }
    }

    /**
     * Private methods
     */
    private boolean checkLocatorState(String methodName, String selector, Object[] args, Function<Locator, Boolean> stateCheck) {
        Locator parsedLocator = byLocator(selector, args);
        logInfo(methodName + ", element --> " + parsedLocator);
        try {
            return stateCheck.apply(parsedLocator);
        } catch (PlaywrightException e) {
            if (e.getMessage().contains("Object doesn't exist")) {
                logInfo("Element already removed: " + selector);
                return false;
            } else {
                throw e;
            }
        }
    }

    private void performOnLocator(String methodName, String selector, Object[] args, Consumer<Locator> action) {
        Locator parsedLocator = byLocator(selector, args);
        logInfo(methodName + ", element --> " + parsedLocator);
        action.accept(parsedLocator);
    }

    private <T> T extractFromLocator(String methodName, String selector, Object[] args, Function<Locator, T> extractor) {
        Locator parsedLocator = byLocator(selector, args);
        logInfo(methodName + ", element --> " + parsedLocator);
        return extractor.apply(parsedLocator);
    }

    /**
     * Human-like methods
     */
    protected void humanPause(int minMs, int maxMs) {
        int delay = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
        try {
            if (ThreadLocalRandom.current().nextInt(100) < 40) {
                randomMouseAndScroll();
            }
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    protected void humanClick(Locator locator) {
        try {
            locator.scrollIntoViewIfNeeded();
            locator.hover();
            Thread.sleep(ThreadLocalRandom.current().nextInt(200, 700));

            if (ThreadLocalRandom.current().nextInt(100) < 30) {
                BoundingBox box = locator.boundingBox();
                if (box != null) {
                    double offsetX = ThreadLocalRandom.current().nextDouble(3, Math.max(4, Math.min(12, box.width / 6)));
                    double offsetY = ThreadLocalRandom.current().nextDouble(3, Math.max(4, Math.min(12, box.height / 6)));
                    getPage().mouse().move(box.x + box.width / 2 + offsetX, box.y + box.height / 2 + offsetY);
                    Thread.sleep(ThreadLocalRandom.current().nextInt(120, 400));
                }
            }

            if (ThreadLocalRandom.current().nextInt(100) < 20) {
                locator.dispatchEvent("mousedown");
                Thread.sleep(ThreadLocalRandom.current().nextInt(60, 180));
                locator.dispatchEvent("mouseup");
            } else {
                locator.click(new Locator.ClickOptions().setButton(MouseButton.LEFT));
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            locator.click();
        }
    }

    protected void randomMouseAndScroll() {
        var vp = getPage().viewportSize();
        int vw = (vp != null ? vp.width : 1366);
        int vh = (vp != null ? vp.height : 768);

        int x1 = ThreadLocalRandom.current().nextInt(80, Math.max(120, vw - 80));
        int y1 = ThreadLocalRandom.current().nextInt(100, Math.max(160, vh - 100));
        int x2 = ThreadLocalRandom.current().nextInt(80, Math.max(120, vw - 80));
        int y2 = ThreadLocalRandom.current().nextInt(100, Math.max(160, vh - 100));

        int steps = ThreadLocalRandom.current().nextInt(2, 5);
        for (int s = 1; s <= steps; s++) {
            double t = (double) s / steps;
            double cx = x1 + (x2 - x1) * t;
            double cy = y1 + (y2 - y1) * t;
            getPage().mouse().move(cx, cy);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(40, 120));
            } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }

        int dy = ThreadLocalRandom.current().nextInt(120, 480);
        if (ThreadLocalRandom.current().nextInt(100) < 40) dy = -dy;
        getPage().mouse().wheel(0, dy);
    }

    protected String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
