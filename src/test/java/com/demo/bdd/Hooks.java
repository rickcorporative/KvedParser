package com.demo.bdd;

import com.demo.core.logger.DefaultLogger;
import com.demo.core.allure.AllureTools;
import com.demo.core.config.PlaywrightConfig;
import com.demo.core.config.PlaywrightHolder;
import com.demo.data.GlobalContext;
import com.demo.data.User;
import com.demo.utils.Constants;
import com.demo.utils.Generator;
import com.demo.utils.PlaywrightTools;
import com.demo.utils.PropertyLoader;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class Hooks extends DefaultLogger {

    protected PropertyLoader propertyLoader;
    protected ThreadLocal<Scenario> scenario = new ThreadLocal<>();
    private HashMap<String, String> passwordsForUsers = new HashMap<>();

    @Before(order = 1)
    public void beforeScenario(Scenario scenario) {
        this.scenario.set(scenario);
        String scenarioName = extractCodePrefix(scenario.getName());
        Thread.currentThread().setName(scenarioName);
        PlaywrightConfig.setScenarioName(scenarioName);

        loadSettingsFromFile();
        loadConstantsFromFile(System.getProperty("environment", "google"));
        loadUsersFromFile();

        PlaywrightConfig.createBrowserConfig();

        configLog(scenarioName);
        logInfo("Opening page: " + Constants.LOGIN_URL);

        PlaywrightTools.openUrl(Constants.LOGIN_URL);

        renameScenarioAllureWithArg(scenarioName);
    }

    @After
    public synchronized void tearDown() {
        if (scenario.get().isFailed()) {
            byte[] screenshot = AllureTools.attachScreenshot(PlaywrightConfig.getPage());
            scenario.get().attach(screenshot, "image/png", "Failure Screenshot");
            scenario.get().attach(AllureTools.attachLogFile(), "text/plain", "Failure Log File");
            scenario.get().attach(findAndReadVideo(extractCodePrefix(scenario.get().getName())), "video/webm", "Failure Video");
        }

        logInfo("Closing Playwright browser...");
        PlaywrightConfig.closeBrowser();
        logInfo("Playwright browser closed!");
        scenario.remove();
    }

    @AfterAll
    public static void afterAll() {
        Constants.globalContext.remove();
        PlaywrightHolder.shutdown();
    }

    private void loadSettingsFromFile() {
        propertyLoader = new PropertyLoader("settings.properties");
        Constants.NANO_TIMEOUT = Integer.parseInt(propertyLoader.get("NANO_TIMEOUT"));
        Constants.MICRO_TIMEOUT = Integer.parseInt(propertyLoader.get("MICRO_TIMEOUT"));
        Constants.MINI_TIMEOUT = Integer.parseInt(propertyLoader.get("MINI_TIMEOUT"));
        Constants.SMALL_TIMEOUT = Integer.parseInt(propertyLoader.get("SMALL_TIMEOUT"));
        Constants.BIG_TIMEOUT = Integer.parseInt(propertyLoader.get("BIG_TIMEOUT"));

        Constants.SCREEN_WIDTH = Integer.parseInt(propertyLoader.get("SCREEN_WIDTH"));
        Constants.SCREEN_HEIGHT = Integer.parseInt(propertyLoader.get("SCREEN_HEIGHT"));

        Constants.TIMEOUT_BEFORE_FAIL = Integer.parseInt(propertyLoader.get("TIMEOUT_BEFORE_FAIL"));
    }

    private void loadUsersFromFile() {
        propertyLoader.reloadProperties("users.properties");
        ArrayList<User> users = new ArrayList<>();

        Set<String> keys = propertyLoader.getKeys();
        Set<Integer> userIndices = new TreeSet<>();

        for (String key : keys) {
            if (key.matches("user\\d+\\.mail")) {
                String index = key.replaceAll("user(\\d+)\\.mail", "$1");
                userIndices.add(Integer.parseInt(index));
            }
        }

        for (int i : userIndices) {
            String mailRaw = propertyLoader.get("user" + i + ".mail");
            String email = insertNumbersToEmail(mailRaw, Generator.getRandomStringNumber(6));
            String emailPassword = propertyLoader.get("user" + i + ".password");
            String appPassword = propertyLoader.get("user" + i + ".app.password");

            if (!email.isEmpty()
                    && emailPassword != null && !emailPassword.isEmpty()
                    && appPassword != null && !appPassword.isEmpty()) {

                User user = new User(email, emailPassword, appPassword);

                if (passwordsForUsers.containsKey("user" + i)) {
                    user.setPasswordBase64(passwordsForUsers.get("user" + i));
                } else {
                    logInfo("Password for user " + i + " is not set in properties file " + System.getProperty("environment", "google") + ".properties");
                }

                users.add(user);
            } else {
                logInfo("User " + i + " is not loaded. Check user" + i + ".properties file");
            }
        }
        Constants.globalContext.get().setEmailUsers(users);
    }

    private void loadConstantsFromFile(String environment) {
        Constants.globalContext.set(new GlobalContext());
        propertyLoader.reloadProperties(environment + ".properties");

        Constants.BASE_URL = propertyLoader.get("base.url");
        Constants.LOGIN_URL = Constants.BASE_URL + "/sign_in";
//        Constants.LOGIN_URL = Constants.BASE_URL + "/login";

        String passwordForUser;

        for (int i = 1; propertyLoader.get("user" + i + ".password") != null; i++) {
            passwordForUser = propertyLoader.get("user" + i + ".password");
            passwordsForUsers.put("user" + i, passwordForUser);
        }
    }

    public static String insertNumbersToEmail(String email, String insert) {
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Email doesn't contain '@'");
        }

        String createdEmail = email.substring(0, atIndex) + "+" + insert + email.substring(atIndex);
        log.info("Created email: " + createdEmail);
        return createdEmail;
    }

    private String extractCodePrefix(String input) {
        if (input == null || input.isEmpty()) return "";

        Pattern pattern = Pattern.compile("^([A-Z]+\\d+)");
        Matcher matcher = pattern.matcher(input.trim());

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private byte[] findAndReadVideo(String scenarioName) {
        Path videoDir = Paths.get("target/videos", scenarioName);
        if (!Files.exists(videoDir)) return null;

        try (Stream<Path> files = Files.list(videoDir)) {
            Optional<Path> webmFile = files
                    .filter(path -> path.toString().endsWith(".webm"))
                    .findFirst();

            if (webmFile.isPresent()) {
                try {
                    return Files.readAllBytes(webmFile.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void renameScenarioAllureWithArg(String scenarioName) {
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(scenarioName);
            testResult.setFullName("Scenario: " + scenarioName);
        });
    }
}
