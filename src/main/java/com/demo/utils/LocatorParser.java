package com.demo.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.MissingFormatArgumentException;

public class LocatorParser {
    public static Locator parseLocator(Page page, String pattern, Object... args) {
        String formatted = formatPattern(pattern, args);

        if (formatted.startsWith("/") || formatted.startsWith("//")) {
            return page.locator("xpath=" + formatted);
        }

        return page.locator(formatted);
    }

    private static String formatPattern(String pattern, Object... args) {
        try {
            return String.format(pattern.trim(), args);
        } catch (MissingFormatArgumentException e) {
            throw new MissingFormatArgumentException(
                    "Not enough arguments for pattern: `" + pattern + "`");
        }
    }
}
