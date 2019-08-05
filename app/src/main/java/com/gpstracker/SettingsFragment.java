package com.gpstracker;

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

public class SettingsFragment extends Fragment {

    public final static String TAG = "SettingsFragment";

    private LinearLayout mMapType;
    private TextView mSelectedMapType;

    private Switch mSimulateGps;
    private TextView mDeleteData;

    private DatabaseHelper mDb;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment getInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mMapType = view.findViewById(R.id.map_type);
        mMapType.setOnClickListener(mMapTypeClickListener);

        mSelectedMapType = view.findViewById(R.id.selected_map_type);
        mSelectedMapType.setText(SettingsHandler.getMapType());

        mSimulateGps = view.findViewById(R.id.simulate_gps);
        mSimulateGps.setChecked(SettingsHandler.isGpsSimulationEnabled());
        mSimulateGps.setOnCheckedChangeListener(mSimulateGpsChangeListener);

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
            maps[0] = "Normal";
            maps[1] = "Satellite";
            maps[2] = "Terrain";
            maps[3] = "Hybrid";

            new AlertDialog.Builder(MainActivity.getContext())
                    .setTitle("Select map type")
                    .setItems(maps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SettingsHandler.setMapType(maps[id].toString());
                            mSelectedMapType.setText(maps[id].toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        }
    };

    private TextView.OnClickListener mDeleteCoordinatesClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.getContext())
                    .setTitle("Cancellare tutti i dati?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            mDb.deleteAllTracks();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    };
}
