package com.demo.core.base;

import com.demo.core.logger.DefaultLogger;
import com.demo.core.config.PlaywrightConfig;
import com.demo.utils.Constants;
import com.demo.utils.LocatorParser;
import com.demo.utils.PlaywrightTools;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public class PageTools extends DefaultLogger {
    private final Page page;

    public PageTools() {
        this.page = PlaywrightConfig.getPage();
    }

    public PageTools(Page page) {
        this.page = page;
    }

    protected Page getPage() {
        return page;
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
        return LocatorParser.parseLocator(page, by, args);
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
                Thread.sleep(1000); // ждём 1 секунду
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
                PlaywrightTools.sleep(Constants.NANO_TIMEOUT); // у тебя уже есть этот helper
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
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, locator -> locator.getAttribute(attr));
    }

    protected String getHiddenElementAttributeValue(String attr, String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            if (!locator.isHidden()) {
                throw new AssertionError("Element is not hidden");
            }
            return locator.getAttribute(attr);
        });
    }

    protected String getDisabledElementAttributeValue(String attr, String selector, Object... args) {
        return extractFromLocator(getPreviousMethodNameAsText(), selector, args, locator -> {
            if (!locator.isDisabled()) {
                throw new AssertionError("Element is not disabled");
            }
            return locator.getAttribute(attr);
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
        page.evaluate("el => el.scrollIntoView({block: 'center'})", locator);
    }

    protected ElementHandle getWebElement(String selector, Object... args) {
        Locator locator = byLocator(selector, args);
        return locator.elementHandle();
    }

    /**
     * Work with colors
     */
    protected Path downloadFile(String selector, Object... args) {
        try {
            Locator locator = byLocator(selector, args);
            Download download = page.waitForDownload(locator::click);
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
}