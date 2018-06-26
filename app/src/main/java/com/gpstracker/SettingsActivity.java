package com.gpstracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.gpstracker.R;

public class SettingsActivity extends AppCompatActivity {
    Switch mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        mSettings = findViewById(R.id.simulation_switch);
        mSettings.setOnCheckedChangeListener(settingsListener);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        mSettings.setChecked(prefs.getBoolean("Simulation", false));
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean("Simulation", mSettings.isChecked());
        editor.apply();
    }

    private CompoundButton.OnCheckedChangeListener settingsListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putBoolean("Simulation", mSettings.isChecked());
            editor.apply();
        }
    };
}
