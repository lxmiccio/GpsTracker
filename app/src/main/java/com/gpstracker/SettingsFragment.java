package com.gpstracker;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private SharedPreferences mSharedPreference;

    private TextView mStartStopTracking;
    private TextView mDeleteData;

    private LinearLayout mMapType;
    private TextView mSelectedMapType;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mSharedPreference = MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE);

        mStartStopTracking = view.findViewById(R.id.start_stop_tracking);
        mStartStopTracking.setOnClickListener(startStopTrackingClickListener);

        mDeleteData = view.findViewById(R.id.delete_data);
        mDeleteData.setOnClickListener(deleteCoordinatesClickListener);

        mMapType = view.findViewById(R.id.map_type);
        mMapType.setOnClickListener(mapTypeClickListener);

        mSelectedMapType = view.findViewById(R.id.selected_map_type);
        mSelectedMapType.setText(mSharedPreference.getString("MapType", "Normal"));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private TextView.OnClickListener startStopTrackingClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (GpsService.getInstance().isTracking()) {
                GpsService.getInstance().stopLocationUpdates();
            } else {
                GpsService.getInstance().startLocationUpdates();
            }
        }
    };

    private TextView.OnClickListener deleteCoordinatesClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.getContext())
                    .setTitle("Cancellare tutti i dati?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            DatabaseHelper.getInstance().deleteAllTracks();
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

    private TextView.OnClickListener mapTypeClickListener = new TextView.OnClickListener() {
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
                            SharedPreferences.Editor editor = mSharedPreference.edit();
                            editor.putString("MapType", maps[id].toString());
                            editor.apply();

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
}
