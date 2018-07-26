package br.com.lfdb.particity.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {

    public static boolean isJsonArray(String value) {
        try {
            new JSONArray(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isJsonObject(String value) {
        try {
            new JSONObject(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
