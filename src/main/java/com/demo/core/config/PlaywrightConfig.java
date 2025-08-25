// src/main/java/com/demo/core/config/PlaywrightConfig.java
package com.demo.core.config;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RecordVideoSize;
import com.microsoft.playwright.options.ViewportSize;
import com.demo.utils.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class PlaywrightConfig {

    // ===== ВРЕМЕННАЯ ОБРАТНАЯ СОВМЕСТИМОСТЬ: делегаты в RunContext =====
    public static com.microsoft.playwright.Page getPage() {
        return com.demo.core.runtime.RunContext.getPage();
    }
    public static com.microsoft.playwright.BrowserContext getContext() {
        return com.demo.core.runtime.RunContext.getContext();
    }
    public static void setPage(com.microsoft.playwright.Page page) {
        com.microsoft.playwright.BrowserContext ctx = com.demo.core.runtime.RunContext.getContext();
        com.demo.core.runtime.RunContext.set(page, ctx);
    }
    public static void setContext(com.microsoft.playwright.BrowserContext ctx) {
        com.microsoft.playwright.Page page = com.demo.core.runtime.RunContext.getPage();
        com.demo.core.runtime.RunContext.set(page, ctx);
    }


    private PlaywrightConfig() {}

    // ⚙️ Реюз storageState можно включить флагом: -DstorageState=true
    private static final boolean STORAGE_STATE_ENABLED =
            Boolean.parseBoolean(System.getProperty("storageState", "false"));
    private static final Path STATE_PATH = Paths.get("target/storageState.json");

    private static final double TIMEOUT_MS = Constants.TIMEOUT_BEFORE_FAIL * 1000.0;

    // ========= ФАБРИЧНЫЕ МЕТОДЫ (без хранения состояния) =========

    /** Создаёт новый Browser с детерминированными опциями */
    public static Browser createBrowser(Playwright pw) {
        String browserType = System.getProperty("playwright.browser", "chrome");
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(List.of(
                        "--disable-notifications",
                        "--start-maximized",
                        "--window-size=" + Constants.SCREEN_WIDTH + "," + Constants.SCREEN_HEIGHT
                ))
                .setTimeout(TIMEOUT_MS);

        return switch (browserType.toLowerCase()) {
            case "chrome" -> pw.chromium().launch(options.setChannel("chrome"));
            case "firefox" -> pw.firefox().launch(options);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserType);
        };
    }


    /** Создаёт новый BrowserContext со всеми антидетект-настройками и видео-папкой по сценарию */
    public static BrowserContext createContext(Browser browser, String scenarioName) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // локации/таймзоны с весами
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
        };

        String[] UAS = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
        };

        String locale   = pickWeighted(WEIGHTED_LOCALES, rnd);
        String timezone = pickWeighted(WEIGHTED_TZ, rnd);
        String userAgent= UAS[rnd.nextInt(UAS.length)];

        String acceptLang = switch (locale) {
            case "uk-UA" -> "uk-UA,uk;q=0.9,ru-RU;q=0.8,ru;q=0.7,en-US;q=0.6,en;q=0.5";
            case "ru-RU" -> "ru-RU,ru;q=0.9,uk-UA;q=0.8,uk;q=0.7,en-US;q=0.6,en;q=0.5";
            case "pl-PL" -> "pl-PL,pl;q=0.9,en-US;q=0.7,en;q=0.6";
            case "de-DE" -> "de-DE,de;q=0.9,en-US;q=0.7,en;q=0.6";
            default       -> "en-US,en;q=0.9,ru-RU;q=0.6,uk-UA;q=0.5";
        };

        int vw = choose(rnd, new int[]{1366,1440,1536,1600,1680,1728,1920}) + rnd.nextInt(0, 41);
        int baseH = switch (vw) {
            case 1366 -> 768;
            case 1440,1536 -> 900;
            case 1600,1680 -> 1050;
            case 1728 -> 1117;
            default -> 1080;
        };
        int vh = baseH + rnd.nextInt(0, 31);

        String platform = rnd.nextInt(100) < 85 ? "Win32" : "MacIntel";
        int hwThreads = choose(rnd, new int[]{4,6,8,12});

        Path videoDir = Paths.get("target/videos",
                scenarioName != null && !scenarioName.isEmpty() ? scenarioName : Thread.currentThread().getName());

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
                        "DNT", "1"
                ));

        if (STORAGE_STATE_ENABLED && Files.exists(STATE_PATH)) {
            contextOptions.setStorageStatePath(STATE_PATH);
        }

        BrowserContext context = browser.newContext(contextOptions);

        // антидетект init-script
        context.addInitScript(String.format("""
            Object.defineProperty(navigator,'webdriver',{get:()=>undefined});
            Object.defineProperty(navigator,'languages',{get:()=>['%s','%s'.split('-')[0],'en-US','en']});
            Object.defineProperty(navigator,'plugins',{get:()=>[1,2,3,4]});
            Object.defineProperty(navigator,'platform',{get:()=>'%s'});
            Object.defineProperty(navigator,'hardwareConcurrency',{get:()=>%d});
            try{ matchMedia = () => ({ matches:false }); }catch(e){}
            (()=>{ const gp=WebGLRenderingContext.prototype.getParameter;
              WebGLRenderingContext.prototype.getParameter=function(p){
                if(p===37445) return 'Intel Inc.';
                if(p===37446) return 'ANGLE (Intel, Intel(R) UHD Graphics, D3D11)';
                return gp.call(this,p);
              };
            })();
        """, locale, locale, platform, hwThreads));

        return context;
    }

    /** Создаёт новую страницу и проставляет дефолтные таймауты */
    public static Page createPage(BrowserContext context) {
        Page p = context.newPage();
        p.setDefaultTimeout(TIMEOUT_MS);
        p.setDefaultNavigationTimeout(Math.max(15000, (long) TIMEOUT_MS));
        return p;
    }

    /** Сохраняет storageState (если включено системным флагом) — вызывать в @After при необходимости */
    public static void maybeSaveStorageState(BrowserContext context) {
        if (!STORAGE_STATE_ENABLED || context == null) return;
        try {
            context.storageState(new BrowserContext.StorageStateOptions().setPath(STATE_PATH));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /** Удалить сохранённый storageState вручную */
    public static void deleteStorageState() {
        try {
            if (Files.exists(STATE_PATH)) Files.delete(STATE_PATH);
        } catch (Exception ignore) {}
    }

    // ====== helpers ======
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
