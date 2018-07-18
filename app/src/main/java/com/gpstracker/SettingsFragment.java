package com.gpstracker;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private SharedPreferences mSharedPreference;

    private Switch mSimulation;
    private TextView mDeleteCoordinates;

    private LinearLayout mMapType;
    private TextView mSelectedMapType;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
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

        mSimulation = view.findViewById(R.id.simulation_switch);
        mSimulation.setOnCheckedChangeListener(simulationCheckListener);
        mSimulation.setChecked(mSharedPreference.getBoolean("Simulation", false));

        mDeleteCoordinates = view.findViewById(R.id.delete_coordinates);
        mDeleteCoordinates.setOnClickListener(deleteCoordinatesClickListener);

        mMapType = view.findViewById(R.id.map_type);
        mMapType.setOnClickListener(mapTypeClickListener);

        mSelectedMapType = view.findViewById(R.id.selected_map_type);
        mSelectedMapType.setText(mSharedPreference.getString("MapType", "Normal"));

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    private TextView.OnClickListener deleteCoordinatesClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("SET", "deleteCoordinatesClickListener");
            DatabaseHelper.getInstance().deleteAllCoordinates();
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
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    };

    private CompoundButton.OnCheckedChangeListener simulationCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("SET", "simulationCheckListener");
            SharedPreferences.Editor editor = mSharedPreference.edit();
            editor.putBoolean("Simulation", isChecked);
            editor.apply();
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
