package com.gpstracker;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CoordinatesList extends AppCompatActivity {

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates_list);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_coordinates_list, mobileArray);

        ListView listView = (ListView) findViewById(R.id.coordinates_list);
        listView.setAdapter(adapter);
    }
}
