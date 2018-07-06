package com.gpstracker;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private SharedPreferences mSharedPreference;
    private TextView mDeleteCoordinates;
    private Switch mSimulation;

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

        mSharedPreference = this.getActivity().getPreferences(MODE_PRIVATE);

        mDeleteCoordinates = view.findViewById(R.id.delete_coordinates);
        mDeleteCoordinates.setOnClickListener(deleteCoordinatesClickListener);

        mSimulation = view.findViewById(R.id.simulation_switch);
        mSimulation.setOnCheckedChangeListener(simulationCheckListener);
        mSimulation.setChecked(mSharedPreference.getBoolean("Simulation", false));

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

    public boolean isSimulationEnabled() {
        //return mSharedPreference.getBoolean("Simulation", false);
        return false;
    }

    private TextView.OnClickListener deleteCoordinatesClickListener = new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("SET", "deleteCoordinatesClickListener");
            DatabaseHelper.getInstance().deleteAllCoordinates();
        }
    };

    private CompoundButton.OnCheckedChangeListener simulationCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
