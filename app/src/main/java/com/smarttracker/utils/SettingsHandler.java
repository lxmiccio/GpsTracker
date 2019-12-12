package com.smarttracker.utils;

import android.content.SharedPreferences;

import com.smarttracker.view.activities.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class SettingsHandler {

    public static String getMapType() {
        return MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getString("MapType", "Normal");
    }

    public static void setMapType(String mapType) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("MapType", mapType);
        editor.apply();
    }

    public static boolean isGpsSimulationEnabled() {
        return MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getBoolean("isGpsSimulationEnabled", false);
    }

    public static void setGpsSimulationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putBoolean("isGpsSimulationEnabled", enabled);
        editor.apply();
    }

    public static long getSessionToSimulate() {
        return MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getLong("SessionToSimulate", -1);
    }

    public static void setSessionToSimulate(long sessionId) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putLong("SessionToSimulate", sessionId);
        editor.apply();
    }
}
