package com.jeremyfox.My_Notes.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/19/13
 * Time: 8:04 PM
 */
public class PrefsHelper {

    /**
     * Sets pref.
     *
     * @param context the context
     * @param key the key
     * @param value the value
     */
    public static void setPref(Context context, String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Gets pref.
     *
     * @param context the context
     * @param key the key
     * @return the pref
     */
    public static String getPref(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
    }

}
