package com.demo.utils;

import com.demo.core.logger.DefaultLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;


public class PropertyLoader extends DefaultLogger {

    private final String pathToFile = "src/main/resources/data-properties/";
    private final Properties properties;

    public PropertyLoader() {
        this.properties = loadProperties(pathToFile + "google.properties");
    }

    public PropertyLoader(String fileName) {
        this.properties = loadProperties(pathToFile + fileName);
    }

    public Set<String> getKeys() {
        return properties.stringPropertyNames();
    }

    public void reloadProperties(String fileName) {
        this.properties.clear();
        this.properties.putAll(loadProperties(pathToFile + fileName));
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            logWarn("Missing required property: " + key);
            return null;
        }
        return value.trim();
    }

    private Properties loadProperties(String path) {
        Properties props = new Properties();
        File file = new File(path);

        if (!file.exists()) {
            throw new IllegalStateException("Properties file not found: " + path);
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            props.load(reader);
            logInfo("Successfully loaded properties from: " + path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read properties file: " + path, e);
        }

        return props;
    }

    public synchronized void setAndSave(String key, String value) {
        if (value == null) {
            properties.remove(key);
            logInfo("Deleted property: " + key);
        } else {
            properties.setProperty(key, value.trim());
            logInfo("Updated property: " + key + " = " + value.trim());
        }
        save();
    }

    //Needed to update this method
    private void save() {
        File file = new File(pathToFile + System.getProperty("environment", "google") + ".properties");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("# Updated by PropertyLoader\n");
            writer.write("# " + LocalDateTime.now() + "\n\n");

            Map<String, List<String>> grouped = new LinkedHashMap<>();
            grouped.put("base", new ArrayList<>());
            grouped.put("user2", new ArrayList<>());
            grouped.put("user1", new ArrayList<>());
            grouped.put("other", new ArrayList<>()); // запасная группа

            for (String key : properties.stringPropertyNames()) {
                String line = key + "=" + properties.getProperty(key);
                if (key.startsWith("base.")) {
                    grouped.get("base").add(line);
                } else if (key.startsWith("user2.")) {
                    grouped.get("user2").add(line);
                } else if (key.startsWith("user1.")) {
                    grouped.get("user1").add(line);
                } else {
                    grouped.get("other").add(line);
                }
            }

            for (String group : List.of("base", "user2", "user1", "other")) {
                for (String line : grouped.get(group)) {
                    writer.write(line + "\n");
                }
                writer.write("\n");
            }

            logInfo("Saved grouped properties to: " + file.getPath());

        } catch (IOException e) {
            throw new RuntimeException("Failed to write properties file: " + file.getPath(), e);
        }
    }
}
