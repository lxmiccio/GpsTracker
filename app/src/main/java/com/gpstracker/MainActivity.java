package com.gpstracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final int PHONE_ACCESS_FINE_LOCATION = 4;
    static final int PHONE_ACCESS_COARSE_LOCATION = 5;

    static Context mContext;
    private MapsFragment mMapsFragment;
    private TrackListFragment mTrackListFragment;
    private SettingsFragment mSettingsFragment;

    private GpsService mGpsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Store context
        mContext = this;

        mGpsService = GpsService.getInstance();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                mMapsFragment = MapsFragment.getInstance();
                mMapsFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mMapsFragment)
                        .addToBackStack(MapsFragment.TAG)
                        .commit();
            }
        }

        Log.d("MainActivity", "accessFineLocationPermission is " + isAccessFineLocationPermissionGranted());
        if (!isAccessFineLocationPermissionGranted()) {
            Log.d("MainActivity", "accessFineLocationPermission denied, requesting it");
            requestAccessFineLocationPermission();
        }

        Log.d("MainActivity", "accessCoarseLocationPermission is " + isAccessCoarseLocationPermissionGranted());
        if (!isAccessCoarseLocationPermissionGranted()) {
            Log.d("MainActivity", "accessCoarseLocationPermission denied, requesting it");
            requestAccessCoarseLocationPermission();
        }

        // Import default Tracks from Gpx if available
        if (DatabaseHelper.getInstance().getAllSessions().size() == 0) {
            ArrayList<String> trackFilesName = GpxHandler.getTracksList(getFilesDir() + "/default_tracks");
            for (String trackFileName : trackFilesName) {
                Gpx gpx = GpxHandler.loadGpx(getFilesDir() + "/default_tracks", trackFileName);
                Session session = gpx.getSession();

                DatabaseHelper db = DatabaseHelper.getInstance();
                Track newTrack = db.saveTrack(session.getName(), session.getStartingDate());
                Session newSession = db.saveSession(newTrack, session, session.getPoints().get(session.getPoints().size() - 1).getTime()/1000);

                for (TrackPoint point : session.getPoints()) {
                    db.saveCoordinate(newSession, point);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            //Already in main page
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.START);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction supportFragmentTransaction = supportFragmentManager.beginTransaction();

        // Removes all the fragments except MapsFragment
        while (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        // Stop location updates when the Fragment changes
        if (mGpsService.isTracking()) {
            mGpsService.stopLocationUpdates();
        }

        int id = item.getItemId();
        if (id == R.id.nav_tracks_list) {
            if (mTrackListFragment == null) {
                mTrackListFragment = TrackListFragment.getInstance();
            } else {
                mTrackListFragment.refresh();
            }

            supportFragmentTransaction.replace(R.id.fragment_container, mTrackListFragment)
                    .addToBackStack(TrackListFragment.TAG)
                    .commit();

        } else if (id == R.id.nav_settings) {
            if (mSettingsFragment == null) {
                mSettingsFragment = SettingsFragment.getInstance();
            }

            supportFragmentTransaction.replace(R.id.fragment_container, mSettingsFragment)
                    .addToBackStack(SettingsFragment.TAG)
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isAccessFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAccessFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PHONE_ACCESS_FINE_LOCATION);
    }

    private boolean isAccessCoarseLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAccessCoarseLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PHONE_ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("MainActivity", "Permission request code is " + requestCode);
        switch (requestCode) {
            case PHONE_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Log.d("MainActivity", "Location permission granted");
                }
                return;
            }
        }
    }

    public static Context getContext() {
        return mContext;
    }
}
