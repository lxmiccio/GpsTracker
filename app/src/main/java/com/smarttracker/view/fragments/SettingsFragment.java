package com.smarttracker.view.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.smarttracker.R;
import com.smarttracker.model.Session;
import com.smarttracker.model.db.DatabaseHelper;
import com.smarttracker.services.GpsService;
import com.smarttracker.utils.SettingsHandler;
import com.smarttracker.view.activities.MainActivity;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    public final static String TAG = "SettingsFragment";

    private LinearLayout mMapType;
    private TextView mSelectedMapType;

    private Switch mSimulateGps;

    private LinearLayout mSimulationSpeed;
    private TextView mSelectedSimulationSpeed;

    private TextView mDeleteData;

    private GpsService mGpsService;
    private DatabaseHelper mDb;

    public SettingsFragment() {
        mGpsService = GpsService.getInstance();
        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        mMapType = view.findViewById(R.id.map_type);
        mMapType.setOnClickListener(mMapTypeClickListener);

        mSelectedMapType = view.findViewById(R.id.selected_map_type);
        mSelectedMapType.setText(SettingsHandler.getMapType());

        mSimulateGps = view.findViewById(R.id.simulate_gps);
        mSimulateGps.setChecked(SettingsHandler.isGpsSimulationEnabled());
        mSimulateGps.setOnCheckedChangeListener(mSimulateGpsChangeListener);

        mSimulationSpeed = view.findViewById(R.id.simulation_speed);
        mSimulationSpeed.setOnClickListener(mSimulationSpeedlickListener);

        mSelectedSimulationSpeed = view.findViewById(R.id.selected_simulation_speed);
        mSelectedSimulationSpeed.setText(String.valueOf(SettingsHandler.getSimulationSpeed()));

        mDeleteData = view.findViewById(R.id.delete_data);
        mDeleteData.setOnClickListener(mDeleteCoordinatesClickListener);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private TextView.OnClickListener mMapTypeClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CharSequence maps[] = new CharSequence[4];
            maps[0] = getString(R.string.normal);
            maps[1] = getString(R.string.satellite);
            maps[2] = getString(R.string.terrain);
            maps[3] = getString(R.string.hybrid);

            new AlertDialog.Builder(MainActivity.getContext())
                    .setTitle(R.string.select_map_type)
                    .setItems(maps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SettingsHandler.setMapType(maps[id].toString());
                            mSelectedMapType.setText(maps[id].toString());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    };

    private Switch.OnCheckedChangeListener mSimulateGpsChangeListener = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SettingsHandler.setGpsSimulationEnabled(isChecked);

            if (isChecked) {
                final ArrayList<Session> sessions = mDb.getAllSessions();
                final CharSequence sessionSequence[] = new CharSequence[sessions.size()];
                for (int i = 0; i < sessions.size(); ++i) {
                    sessionSequence[i] = String.valueOf(sessions.get(i).getName());
                }

                new AlertDialog.Builder(MainActivity.getContext())
                        .setTitle(R.string.select_session_to_simulate)
                        .setItems(sessionSequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                SettingsHandler.setSessionToSimulate(sessions.get(id).getId());
                                mGpsService.stopLowRateLocationUpdated();
                                mGpsService.startLowRateLocationUpdated();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                SettingsHandler.setGpsSimulationEnabled(false);
                                mSimulateGps.setChecked(false);
                            }
                        })
                        .setCancelable(false).show();
            } else {
                mGpsService.stopLowRateLocationUpdated();
                mGpsService.startLowRateLocationUpdated();
            }
        }
    };

    private TextView.OnClickListener mSimulationSpeedlickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CharSequence speeds[] = new CharSequence[6];
            speeds[0] = "0.50";
            speeds[1] = "0.75";
            speeds[2] = "1";
            speeds[3] = "2";
            speeds[4] = "5";
            speeds[5] = "15";

            new AlertDialog.Builder(MainActivity.getContext())
                    .setTitle(R.string.simulation_speed)
                    .setItems(speeds, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SettingsHandler.setSimulationSpeed(Float.parseFloat(speeds[id].toString()));
                            mSelectedSimulationSpeed.setText(speeds[id].toString());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    };

    private TextView.OnClickListener mDeleteCoordinatesClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.getContext())
                    .setTitle(R.string.delete_all_data)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            mDb.deleteAllTracks();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    };
}
