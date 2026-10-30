package com.example.emailclient;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the signed-in account without saving its password. */
public class SessionManager {

    private static final String PREFERENCES_NAME = "email_client_session";
    private static final String KEY_SIGNED_IN_EMAIL = "signed_in_email";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        Context appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveSignedInEmail(String email) {
        preferences.edit()
                .putString(KEY_SIGNED_IN_EMAIL, email)
                .apply();
    }

    public String getSignedInEmail() {
        String email = preferences.getString(KEY_SIGNED_IN_EMAIL, null);

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return email;
    }

    public void clearSession() {
        preferences.edit()
                .remove(KEY_SIGNED_IN_EMAIL)
                .apply();
    }
}