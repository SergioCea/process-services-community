package com.sergio.psc;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorkflowWebScriptUtils {
    private WorkflowWebScriptUtils() {
    }

    public static String getAuthenticatedUser() {
        return AuthenticationUtil.getFullyAuthenticatedUser();
    }

    public static Map<String, Object> toVariables(JSONObject json) throws JSONException {
        Map<String, Object> variables = new LinkedHashMap<>();
        for (String key : json.keySet()) {
            variables.put(key, toJavaValue(json.get(key)));
        }
        return variables;
    }

    private static Object toJavaValue(Object value) throws JSONException {
        if (value == JSONObject.NULL) {
            return null;
        }
        if (value instanceof JSONObject) {
            return toVariables((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            List<Object> values = new ArrayList<>();
            for (int index = 0; index < jsonArray.length(); index++) {
                values.add(toJavaValue(jsonArray.get(index)));
            }
            return values;
        }
        return value;
    }
}