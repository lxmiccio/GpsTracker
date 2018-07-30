package com.gpstracker;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CoordinateListFragment.OnFragmentInteractionListener,
        GoogleMapsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        TrackListFragment.OnFragmentInteractionListener {

    static final int SMS_PERMISSION_CODE = 1;
    static final int PHONE_ACCESS_FINE_LOCATION = 4;
    static final int PHONE_ACCESS_COARSE_LOCATION = 5;

    static Context mContext;
    private GoogleMapsFragment mGoogleMapsFragment;
    private CoordinateListFragment mCoordinatesListFragment;
    private TrackListFragment mTrackListFragment;
    private SettingsFragment mSettingsFragment;
    private SmsBroadcastReceiver smsBroadcastReceiver;

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
                mGoogleMapsFragment = GoogleMapsFragment.getInstance();
                mGoogleMapsFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mGoogleMapsFragment).commit();
            }
        }

        Log.d("MainActivity", "smsPermission is " + isSmsPermissionGranted());
        if (!isSmsPermissionGranted()) {
            Log.d("MainActivity", "smsPermission denied, requesting it");
            requestReadAndSendSmsPermission();
        } else {
            Log.d("MainActivity", "smsPermission granted, registering SmsBroadcastReceiver");
            registerSmsBroadcastReceiver();
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
            getSupportFragmentManager().beginTransaction().show(mGoogleMapsFragment).commit();

            mGoogleMapsFragment.setMapType(getMapType());
            mGoogleMapsFragment.refresh();
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
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        android.support.v4.app.FragmentManager supportFragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction supportFragmentTransaction = supportFragmentManager.beginTransaction();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            if (mCoordinatesListFragment == null) {
                mCoordinatesListFragment = CoordinateListFragment.getInstance();
            } else {
                mCoordinatesListFragment.refresh();
            }

            if (supportFragmentManager.getBackStackEntryCount() == 0) {
                supportFragmentTransaction.remove(mGoogleMapsFragment)
                        .addToBackStack(GoogleMapsFragment.TAG)
                        .commit();
            }

            fragmentTransaction.replace(R.id.fragment_container, mCoordinatesListFragment)
                    .commit();
        } else if (id == R.id.nav_gallery) {
            if (mTrackListFragment == null) {
                mTrackListFragment = TrackListFragment.getInstance();
            } else {
                mTrackListFragment.refresh();
            }

            if (supportFragmentManager.getBackStackEntryCount() == 0) {
                supportFragmentTransaction.remove(mGoogleMapsFragment)
                        .addToBackStack(GoogleMapsFragment.TAG)
                        .commit();
            }

            fragmentTransaction.replace(R.id.fragment_container, mTrackListFragment)
                    .commit();

        } else if (id == R.id.nav_settings) {
            if (mSettingsFragment == null) {
                mSettingsFragment = SettingsFragment.getInstance();
            }

            if (supportFragmentManager.getBackStackEntryCount() == 0) {
                supportFragmentTransaction.remove(mGoogleMapsFragment)
                        .addToBackStack(GoogleMapsFragment.TAG)
                        .commit();
            }

            fragmentTransaction.replace(R.id.fragment_container, mSettingsFragment)
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startRecording() {
        mGpsService.startLocationUpdates();
    }

    private void stopRecording() {
        mGpsService.stopLocationUpdates();
    }

    private boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
    }

    private boolean isAccessFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAccessFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PHONE_ACCESS_FINE_LOCATION);
    }

    private boolean isAccessCoarseLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAccessCoarseLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PHONE_ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SMS_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    registerSmsBroadcastReceiver();
                } else {
                    // Permission denied
                    unregisterReceiver(smsBroadcastReceiver);
                }
                return;
            }
        }
    }

    private void registerSmsBroadcastReceiver() {
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        smsBroadcastReceiver.setSmsListener(new SmsListener() {
            @Override
            public void onSmsReceived(String sender, String text) {
                Log.d("MainActivity", "sender is " + sender + ", message is " + text);
                if (text.equals("enable")) {
                    startRecording();
                } else if (text.equals("disable")) {
                    stopRecording();
                } else {
                    Log.d("MainActivity", "Message text ignored");
                }
            }
        });
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private boolean isSimulationEnabled() {
        return getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE).getBoolean("Simulation", false);
    }

    private String getMapType() {
        return getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE).getString("MapType", "Normal");
    }
}