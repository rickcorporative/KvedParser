package com.demo.core.logger;

import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class DefaultLogger {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Logger staticLogger = LoggerFactory.getLogger(DefaultLogger.class);
    private LogType logType;
    private final java.util.logging.Logger log = java.util.logging.Logger.getLogger(this.getClass().getName());

    @Step("{message}")
    private void log(String message) {
        switch (logType) {
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
            default:
                logger.info(message);
        }
    }

    private String formatLogMessage(String message) {
        return message.contains("%")
                ? message.replaceAll("%", "%%")
                : message;
    }

    public void logInfo(String message, Object... args) {
        logType = LogType.INFO;
        log(Thread.currentThread().getName() + " | " + String.format(formatLogMessage(message), args));
    }

    public static void logStaticInfo(String message, Object... args) {
        staticLogger.info(message, args);
    }

    public void logWarn(String message, Object... args) {
        logType = LogType.WARN;
        log(Thread.currentThread().getName() + " | " + String.format(formatLogMessage(message), args));
    }

    public static void logStaticWarn(String message, Object... args) {
        staticLogger.warn(message, args);
    }

    public void logError(String message, Object... args) {
        logType = LogType.ERROR;
        log(Thread.currentThread().getName() + " | " + String.format(formatLogMessage(message), args));
    }

    public static void logStaticError(String message, Object... args) {
        staticLogger.error(message, args);
    }

    public static void logStaticError(String message, Throwable t, Object... args) {
        staticLogger.error(String.format(message, args), t);
    }

    private enum LogType {
        INFO, WARN, ERROR
    }

    protected void logInFile(String message, Object... args) {
        if (System.getProperty("inFile", "false").equals("true")) {
            log.log(Level.INFO, message);
        }
    }

    protected void configLog(String testName) {
        if (System.getProperty("inFile", "false").equals("true")) {
            FileHandler fh = null;   // true forces append mode
            try {
                File file = new File("Files/Logs");
                if (!file.exists()) {
                    file.mkdir();
                }
                fh = new FileHandler("Files/Logs/" + testName + ".txt", true);
            } catch (IOException e) {
                log.severe("Failed to configure file logger for test: " + testName + " | " + e.getMessage());
            }
            SimpleFormatter sf = new SimpleFormatter();

            fh.setFormatter(sf);
            log.addHandler(fh);
        }
    }
}
