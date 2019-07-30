package com.gpstracker;

import static android.content.Context.MODE_PRIVATE;

public class SettingsHandler {
    public static boolean isGpsSimulationEnabled() {
        return true;
        //return MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getBoolean("isGpsSimulationEnabled", false);
    }
}
