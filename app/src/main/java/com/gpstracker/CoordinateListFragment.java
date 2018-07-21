package com.gpstracker;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class CoordinateListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private CoordinateAdapter mCoordinateAdapter;
    private ListView mListView;

    public CoordinateListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CoordinateListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoordinateListFragment newInstance() {
        CoordinateListFragment fragment = new CoordinateListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_coordinate_list, container, false);

        ArrayList<TrackPoint> points = DatabaseHelper.getInstance().getAllCoordinates();
        mCoordinateAdapter = new CoordinateAdapter(points, MainActivity.getContext());
        mListView = view.findViewById(R.id.coordinates_list);
        mListView.setAdapter(mCoordinateAdapter);

        return view;
    }

    public void refresh() {
        ArrayList<TrackPoint> points = DatabaseHelper.getInstance().getAllCoordinates();
        mCoordinateAdapter = new CoordinateAdapter(points, MainActivity.getContext());
        mListView.setAdapter(mCoordinateAdapter);
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
