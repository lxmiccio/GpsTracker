package com.gpstracker;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class CoordinatesList extends ToolbarActivity {
    private CoordinateAdapter mCoordinateAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates_list);

        ArrayList<TrackPoint> points = DatabaseHelper.getInstance().getAllCoordinates();

        mCoordinateAdapter = new CoordinateAdapter(points, getApplicationContext());

        mListView = findViewById(R.id.coordinates_list);
        mListView.setAdapter(mCoordinateAdapter);
    }
}
