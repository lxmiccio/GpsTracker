package com.smarttracker.utils;

import android.content.SharedPreferences;

import com.smarttracker.view.activities.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class SettingsHandler {

    private static final String SHARED_PREFERENCE_NAME = "settings";
    private static final String MAP_TYPE_KEY = "MapType";
    private static final String GPS_SIMULATION_ENABLED_KEY = "GpsSimulationEnabled";
    private static final String GPS_SESSION_TO_SIMULATE_KEY = "GpsSessionToSimulate";
    private static final String SIMULATION_SPEED_KEY = "SimulationSpeed";

    public static String getMapType() {
        return MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getString(MAP_TYPE_KEY, "Normale");
    }

    public static void setMapType(String mapType) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putString(MAP_TYPE_KEY, mapType);
        editor.apply();
    }

    public static boolean isGpsSimulationEnabled() {
        return MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getBoolean(GPS_SIMULATION_ENABLED_KEY, false);
    }

    public static void setGpsSimulationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(GPS_SIMULATION_ENABLED_KEY, enabled);
        editor.apply();
    }

    public static long getSessionToSimulate() {
        return MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getLong(GPS_SESSION_TO_SIMULATE_KEY, -1);
    }

    public static void setSessionToSimulate(long sessionId) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putLong(GPS_SESSION_TO_SIMULATE_KEY, sessionId);
        editor.apply();
    }

    public static float getSimulationSpeed() {
        return MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getFloat(SIMULATION_SPEED_KEY, 1);
    }

    public static void setSimulationSpeed(float speed) {
        SharedPreferences.Editor editor = MainActivity.getContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putFloat(SIMULATION_SPEED_KEY, speed);
        editor.apply();
    }
}
