package com.gpstracker;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.DatabaseHelper;

import java.util.ArrayList;

public class CoordinatesList extends AppCompatActivity {
    private CoordinateAdapter mCoordinateAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates_list);

        DatabaseHelper db = new DatabaseHelper(this);
        ArrayList<TrackPoint> points = db.getAllCoordinates();

        mCoordinateAdapter = new CoordinateAdapter(points, getApplicationContext());

        mListView = findViewById(R.id.coordinates_list);
        mListView.setAdapter(mCoordinateAdapter);
    }
}
