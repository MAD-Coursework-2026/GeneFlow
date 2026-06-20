package com.geneflow.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Tiny SharedPreferences wrapper for app state (seeding flag + current user). */
public class Session {

    private static final String PREF = "geneflow_prefs";
    private static final String KEY_SEEDED = "seeded";
    private static final String KEY_EMAIL = "current_email";

    private final SharedPreferences sp;

    public Session(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isSeeded() { return sp.getBoolean(KEY_SEEDED, false); }
    public void setSeeded(boolean v) { sp.edit().putBoolean(KEY_SEEDED, v).apply(); }

    public void setCurrentEmail(String email) { sp.edit().putString(KEY_EMAIL, email).apply(); }
    public String getCurrentEmail() { return sp.getString(KEY_EMAIL, ""); }
}
