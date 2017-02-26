package com.alex.telemetry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements GoogleMapsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    GoogleMapsFragment googleMapsFragment;
    FloatingActionButton startRecording;
    FloatingActionButton endRecording;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Retrieve data from the intent
            Location location = intent.getBundleExtra("Location").getParcelable("location");
            if (location != null && googleMapsFragment != null) {
                googleMapsFragment.addLocation(location);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startRecording = (FloatingActionButton) findViewById(R.id.fab);
        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
                startService(new Intent(getBaseContext(), GpsService.class));
                startRecording.hide();
                endRecording.show();
            }
        });

        endRecording = (FloatingActionButton) findViewById(R.id.fab1);
        endRecording.hide();
        endRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.track_save_message)
                        .setPositiveButton(R.string.track_save_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Save the track and delete the route on the map
                                GpxHandler.saveGpx(getFilesDir(), "Mugello", googleMapsFragment.getLocations());

//                                Gpx read = null;
//                                try {
//                                    read = GpxHandler.loadGpx(getFilesDir(), "Mugello");
//                                } catch (Exception e) {
//                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                                }
                            }
                        })
                        .setNegativeButton(R.string.track_save_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Delete the route on the map
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();

                startRecording.show();
                endRecording.hide();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (findViewById(R.id.google_maps_fragment_container) != null) {
            if (savedInstanceState == null) {
                googleMapsFragment = GoogleMapsFragment.newInstance();
                googleMapsFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.google_maps_fragment_container, googleMapsFragment).commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
