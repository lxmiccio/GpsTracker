package com.smarttracker.view.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import com.smarttracker.R;
import com.smarttracker.model.Gpx;
import com.smarttracker.model.Session;
import com.smarttracker.model.Track;
import com.smarttracker.model.TrackPoint;
import com.smarttracker.model.db.DatabaseHelper;
import com.smarttracker.services.GpsService;
import com.smarttracker.utils.GpxHandler;
import com.smarttracker.view.fragments.RecordingFragment;
import com.smarttracker.view.fragments.SettingsFragment;
import com.smarttracker.view.fragments.TrackListFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final public int PHONE_ACCESS_FINE_LOCATION = 4;
    static final public int PHONE_ACCESS_COARSE_LOCATION = 5;

    static private Context mContext;

    private RecordingFragment mRecordingFragment;
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

        // Store context
        mContext = this;

        // Start low rate location updated
        mGpsService = GpsService.getInstance();
        mGpsService.startLowRateLocationUpdated();

        // Add RecordingFragment into the FragmentContainer
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                mRecordingFragment = RecordingFragment.getInstance();
                mRecordingFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mRecordingFragment)
                        .addToBackStack(RecordingFragment.TAG)
                        .commit();
            }
        }

        // Import default Tracks from Gpx if available
        if (DatabaseHelper.getInstance().getAllSessions().size() == 0) {
            ArrayList<String> trackFilesName = GpxHandler.getTracksList(getFilesDir() + "/default_tracks");
            for (String trackFileName : trackFilesName) {
                Gpx gpx = GpxHandler.loadGpx(getFilesDir() + "/default_tracks", trackFileName);
                Session session = gpx.getSession();

                DatabaseHelper db = DatabaseHelper.getInstance();
                Track newTrack = db.saveTrack(session.getName(), session.getStartingDate());
                Session newSession = db.saveSession(newTrack, session, session.getPoints().get(session.getPoints().size() - 1).getTime() / 1000);

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
            // Close the Drawer
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            // Remove a Fragment except RecordingFragment
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            // Already in MainActivity
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

        // Removes all the fragments except RecordingFragment
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

            // Navigate to TrackListFragment
            supportFragmentTransaction.replace(R.id.fragment_container, mTrackListFragment)
                    .addToBackStack(TrackListFragment.TAG)
                    .commit();

        } else if (id == R.id.nav_settings) {
            if (mSettingsFragment == null) {
                mSettingsFragment = new SettingsFragment();
            }

            // Navigate to SettingsFragment
            supportFragmentTransaction.replace(R.id.fragment_container, mSettingsFragment)
                    .addToBackStack(SettingsFragment.TAG)
                    .commit();
        }

        // Close the Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                    mGpsService.startLowRateLocationUpdated();
                }
                return;
            }
        }
    }

    public static Context getContext() {
        return mContext;
    }
}
