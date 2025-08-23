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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
            boolean isHeadless = ThreadLocalRandom.current().nextInt(100) < 70;
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
        if (contextThreadLocal.get() != null) return contextThreadLocal.get();

        String scenarioName = getScenarioName();
        Path videoDir = Paths.get("target/videos", scenarioName != null ? scenarioName : Thread.currentThread().getName());
        Path statePath = Paths.get("target/storageState.json");
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // --- 1) Пулы с «весами» (больше шансов на UA/UA/EU) ---
        String[][] WEIGHTED_LOCALES = {
                {"uk-UA","5"}, {"ru-RU","4"}, {"en-US","3"}, {"pl-PL","2"}, {"de-DE","2"},
                {"fr-FR","1"}, {"es-ES","1"}, {"it-IT","1"}, {"nl-NL","1"}, {"ro-RO","1"},
                {"cs-CZ","1"}, {"hu-HU","1"}, {"sk-SK","1"}, {"bg-BG","1"}, {"lt-LT","1"},
                {"lv-LV","1"}, {"et-EE","1"}, {"pt-PT","1"}, {"pt-BR","1"}
        };
        String[][] WEIGHTED_TZ = {
                {"Europe/Kiev","5"}, {"Europe/Warsaw","3"}, {"Europe/Berlin","2"}, {"Europe/Paris","2"},
                {"Europe/Amsterdam","1"}, {"Europe/Prague","1"}, {"Europe/Budapest","1"}, {"Europe/Sofia","1"},
                {"Europe/Vilnius","1"}, {"Europe/Riga","1"}, {"Europe/Tallinn","1"}, {"Europe/Bucharest","1"},
                {"Europe/Bratislava","1"}, {"Europe/Helsinki","1"}, {"Europe/Stockholm","1"}, {"Europe/Oslo","1"},
                {"Europe/Copenhagen","1"}, {"Europe/London","1"}, {"Europe/Dublin","1"}
                // (при желании добавь America/New_York и т.п.)
        };

        // Реалистичные User-Agent’ы (десктоп Chrome/Edge последних релизов)
        String[] UAS = new String[] {
                // Win10/11 + Chrome
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
                // Win10/11 + Edge
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",
                // macOS + Chrome
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
        };

        String locale = pickWeighted(WEIGHTED_LOCALES, rnd);
        String timezone = pickWeighted(WEIGHTED_TZ, rnd);
        String userAgent = UAS[rnd.nextInt(UAS.length)];

        // Accept-Language согласуем с выбранной локалью
        String acceptLang = switch (locale) {
            case "uk-UA" -> "uk-UA,uk;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6,en;q=0.5";
            case "ru-RU" -> "ru-RU,ru;q=0.9,uk-UA;q=0.8,uk;q=0.7,en-US;q=0.6,en;q=0.5";
            case "pl-PL" -> "pl-PL,pl;q=0.9,en-US;q=0.7,en;q=0.6";
            case "de-DE" -> "de-DE,de;q=0.9,en-US;q=0.7,en;q=0.6";
            default       -> "en-US,en;q=0.9,ru-RU;q=0.6,uk-UA;q=0.5";
        };

        // Случайный реалистичный viewport (±ноутбуки/десктопы)
        int vw = choose(rnd, new int[]{1366,1440,1536,1600,1680,1728,1920});
        int vh = switch (vw) {
            case 1366 -> 768;
            case 1440,1536 -> 900;
            case 1600,1680 -> 1050;
            case 1728 -> 1117;
            default -> 1080; // для 1920
        };
        vw += rnd.nextInt(0, 41); // небольшой «шум»
        vh += rnd.nextInt(0, 31);

        // Платформа и «железо»
        String platform = rnd.nextInt(100) < 85 ? "Win32" : "MacIntel";
        int hwThreads = choose(rnd, new int[]{4,6,8,12}); // ядра/потоки

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(new ViewportSize(vw, vh))
                .setRecordVideoSize(new RecordVideoSize(vw, vh))
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true)
                .setRecordVideoDir(videoDir)
                .setUserAgent(userAgent)
                .setLocale(locale)
                .setTimezoneId(timezone)
                .setExtraHTTPHeaders(Map.of(
                        "Accept-Language", acceptLang,
                        "DNT", "1" // Do‑Not‑Track как у части юзеров
                ));

        if (Files.exists(statePath)) {
            contextOptions.setStorageStatePath(statePath);
        }

        BrowserContext context = getBrowser().newContext(contextOptions);

        // --- 2) Init‑скрипты (одним блоком) ---
        context.addInitScript("""
        // webdriver → undefined
        Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
        // languages
        Object.defineProperty(navigator, 'languages', { get: () => [ '%s', '%s'.split('-')[0], 'en-US', 'en' ] });
        // plugins
        Object.defineProperty(navigator, 'plugins', { get: () => [1,2,3,4] });
        // platform
        Object.defineProperty(navigator, 'platform', { get: () => '%s' });
        // hardwareConcurrency
        Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => %d });
        // color scheme (как у большинства)
        try { matchMedia = () => ({ matches: false }); } catch(e){}
        // WebGL spoof
        (() => {
          const getParameter = WebGLRenderingContext.prototype.getParameter;
          WebGLRenderingContext.prototype.getParameter = function(param) {
            if (param === 37445) return 'Intel Inc.';            // UNMASKED_VENDOR_WEBGL
            if (param === 37446) return 'ANGLE (Intel, Intel(R) UHD Graphics, D3D11)';
            return getParameter.call(this, param);
          };
        })();
        """.formatted(locale, locale, platform, hwThreads));

        // Сохраняем стейт при закрытии
        context.onClose(c -> {
            try {
                context.storageState(new BrowserContext.StorageStateOptions().setPath(statePath));
            } catch (Exception e) { e.printStackTrace(); }
        });

        contextThreadLocal.set(context);
        return context;
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

    private static String pickWeighted(String[][] items, ThreadLocalRandom rnd) {
        int sum = 0;
        for (String[] it : items) sum += Integer.parseInt(it[1]);
        int r = rnd.nextInt(1, sum + 1), acc = 0;
        for (String[] it : items) {
            acc += Integer.parseInt(it[1]);
            if (r <= acc) return it[0];
        }
        return items[0][0];
    }
    private static int choose(ThreadLocalRandom rnd, int[] values) {
        return values[rnd.nextInt(values.length)];
    }
}
