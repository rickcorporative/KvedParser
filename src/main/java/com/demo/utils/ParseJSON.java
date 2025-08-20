package com.demo.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;

import java.io.FileReader;

public class ParseJSON {

    private static JsonArray parseToJSONArray(String jsonLocation) throws Exception {
        try (FileReader reader = new FileReader(jsonLocation)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                throw new IllegalStateException("Expected JSON array at root");
            }
            return root.getAsJsonArray();
        }
    }

    private static JsonObject getData(String name, String jsonLocation) throws Exception {
        JsonArray array = parseToJSONArray(jsonLocation);
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            JsonElement value = object.get(name);
            if (value != null && value.isJsonObject()) {
                return value.getAsJsonObject();
            }
        }
        return null;
    }

    public static String getValueByJsonPath(String jsonText, String jsonPath) {
        try {
            return JsonPath.read(jsonText, jsonPath);
        }
        catch(Exception e) {
            return e.getMessage();
        }
    }
}
