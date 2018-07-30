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

    private DatabaseHelper mDb;

    public CoordinateListFragment() {
        // Required empty public constructor
        mDb = DatabaseHelper.getInstance();
    }

    public static CoordinateListFragment getInstance() {
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

        ArrayList<TrackPoint> points = mDb.getAllCoordinates();
        mCoordinateAdapter = new CoordinateAdapter(points, MainActivity.getContext());
        mListView = view.findViewById(R.id.coordinates_list);
        mListView.setAdapter(mCoordinateAdapter);

        return view;
    }

    public void refresh() {
        ArrayList<TrackPoint> points = mDb.getAllCoordinates();
        mCoordinateAdapter = new CoordinateAdapter(points, MainActivity.getContext());
        mListView.setAdapter(mCoordinateAdapter);
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
}
