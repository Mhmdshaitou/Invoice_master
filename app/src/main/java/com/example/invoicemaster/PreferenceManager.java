package com.example.invoicemaster;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_USER_EMAIL = "userEmail";
    private static final String PREF_USER_ID = "userId";
    private static final String PREF_USER_ROLE = "userRole";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setUserEmail(String email) {
        editor.putString(PREF_USER_EMAIL, email);
        editor.apply();
    }
    public void setUserRole(String role) {
        editor.putString(PREF_USER_ROLE, role);
        editor.apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(PREF_USER_ROLE, null);
    }
    public String getUserEmail() {
        return sharedPreferences.getString(PREF_USER_EMAIL, null);
    }

    public void setUserId(String id) {
        editor.putString(PREF_USER_ID, id);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(PREF_USER_ID, null);
    }

    public void clearUserData() {
        editor.remove(PREF_USER_EMAIL);
        editor.remove(PREF_USER_ID);
        editor.apply();
    }
}