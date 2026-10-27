package com.example.emailclient;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public class AuthStore {

    private static final String PREF = "auth_store";
    private static final String K_USERS = "users";
    private static final String K_SESSION = "session";

    private static AuthStore instance;

    private final SharedPreferences prefs;

    private AuthStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static synchronized AuthStore get(Context ctx) {
        if (instance == null) instance = new AuthStore(ctx);
        return instance;
    }

    public synchronized boolean register(String email, String password) {
        String key = norm(email);
        JSONObject users = users();
        if (users.has(key)) return false;
        try {
            users.put(key, hash(password));
        } catch (JSONException e) {
            return false;
        }
        prefs.edit().putString(K_USERS, users.toString()).apply();
        return true;
    }

    public synchronized boolean login(String email, String password) {
        String key = norm(email);
        String stored = users().optString(key, null);
        if (stored == null || !stored.equals(hash(password))) return false;
        prefs.edit().putString(K_SESSION, key).apply();
        return true;
    }

    public boolean isLoggedIn() {
        return prefs.getString(K_SESSION, null) != null;
    }

    public String currentEmail() {
        return prefs.getString(K_SESSION, null);
    }

    public void logout() {
        prefs.edit().remove(K_SESSION).apply();
    }

    private JSONObject users() {
        try {
            return new JSONObject(prefs.getString(K_USERS, "{}"));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private static String norm(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
