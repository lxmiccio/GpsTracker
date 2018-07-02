package com.gpstracker;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleMapsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener,
        CoordinateList.OnFragmentInteractionListener{

    private GoogleMapsFragment googleMapsFragment;
    private GpsService mGpsService;

    private CoordinateList mCoordinatesList;

    CharSequence maps[];

    private boolean mSimulation;
    GpsSimulator mGpsSimulator;
    static Context mContext;

    static final int SMS_PERMISSION_CODE = 1;
    static final int PHONE_NUMBERS_PERMISSION_CODE = 2;
    static final int PHONE_STATE_PERMISSION_CODE = 3;
    static final int PHONE_ACCESS_FINE_LOCATION = 4;
    static final int PHONE_ACCESS_COARSE_LOCATION = 5;
    private SmsBroadcastReceiver smsBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        NavigationView navigationView = findViewById(R.id.nav_view_0);
        navigationView.setNavigationItemSelectedListener(this);

        //Store context
        mContext = this;

        mSimulation = false;
        mGpsSimulator = new GpsSimulator();

        if (findViewById(R.id.google_maps_fragment_container) != null) {
            if (savedInstanceState == null) {
                googleMapsFragment = GoogleMapsFragment.newInstance();
                googleMapsFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.google_maps_fragment_container, googleMapsFragment).commit();
            }
        }

        Log.d("MainActivity", "smsPermission is " + isSmsPermissionGranted());
        if(!isSmsPermissionGranted()) {
            Log.d("MainActivity", "smsPermission denied, requesting it");
            requestReadAndSendSmsPermission();
        }
        else {
            Log.d("MainActivity", "smsPermission granted, registering SmsBroadcastReceiver");
            registerSmsBroadcastReceiver();
        }

        Log.d("MainActivity", "readPhoneNumberPermission is " + isReadPhoneNumberPermissionGranted());
        if(!isReadPhoneNumberPermissionGranted()) {
            Log.d("MainActivity", "readPhoneNumberPermission denied, requesting it");
            requestReadPhoneNumberPermission();
        }
        else {
            Log.d("MainActivity", "readPhoneNumberPermission granted");
            smsBroadcastReceiver.getNumber();
        }

        Log.d("MainActivity", "accessFineLocationPermission is " + isAccessFineLocationPermissionGranted());
        if(!isAccessFineLocationPermissionGranted()) {
            Log.d("MainActivity", "accessFineLocationPermission denied, requesting it");
            requestAccessFineLocationPermission();
        }

        Log.d("MainActivity", "accessCoarseLocationPermission is " + isAccessCoarseLocationPermissionGranted());
        if(!isAccessCoarseLocationPermissionGranted()) {
            Log.d("MainActivity", "accessCoarseLocationPermission denied, requesting it");
            requestAccessCoarseLocationPermission();
        }

        Log.d("MainActivity", "readPhoneStatePermission is " + isReadPhoneStatePermissionGranted());
        if(!isReadPhoneStatePermissionGranted()) {
            Log.d("MainActivity", "readPhoneStatePermission denied, requesting it");
            requestReadPhoneStatePermission();
        }
        else {
            Log.d("MainActivity", "readPhoneStatePermission granted");
            smsBroadcastReceiver.getNumber();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("BK", "getFragmentManager().getBackStackEntryCount() is " + getFragmentManager().getBackStackEntryCount());
        Log.d("BK", "getSupportFragmentManager().getBackStackEntryCount() is " + getSupportFragmentManager().getBackStackEntryCount());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            Log.d("BK", "drawer.closeDrawer(GravityCompat.START)");
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        }
        else if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
            getSupportFragmentManager().beginTransaction().show(googleMapsFragment).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.google_maps_fragment_container, googleMapsFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.toolbar_load_gpx) {
//            ArrayList<String> tracksList = GpxHandler.getTracksList(getFilesDir());
//            if (tracksList != null && tracksList.size() > 0) {
//                tracks = tracksList.toArray(new CharSequence[tracksList.size()]);
//                // TODO set gpx png left to track
//
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("Pick a track")
//                        .setItems(tracks, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int id) {
//                                String track = tracks[id].toString();
//
//                                try {
//                                    Gpx gpx = GpxHandler.loadGpx(getFilesDir(), track);
//                                    googleMapsFragment.loadGpx(gpx);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int idd) {
//                                dialog.dismiss();
//                            }
//                        }).show();
//            }
//
//            return true;
//        }
//
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        android.support.v4.app.FragmentManager supportFragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction supportFragmentTransaction = supportFragmentManager.beginTransaction();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            if(mCoordinatesList == null) {
                mCoordinatesList = new CoordinateList();
            }

            supportFragmentTransaction.remove(googleMapsFragment)
                    .addToBackStack(GoogleMapsFragment.TAG)
                    .commit();

            fragmentTransaction.replace(R.id.google_maps_fragment_container, mCoordinatesList)
                    .commit();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static Context getContext() {
        return mContext;
    }

    private View.OnClickListener changeMapTypeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            maps = new CharSequence[3];
            maps[0] = "Normal";
            maps[1] = "Satellite";
            maps[2] = "Terrain";
            // TODO set image left to type name

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Select map type")
                    .setItems(maps, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            googleMapsFragment.setMapType(maps[id].toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogg, int idd) {
                            dialogg.dismiss();
                        }
                    }).show();
        }
    };

    //Other method
//    awesomeButton.setOnClickListener(new AwesomeButtonClick());
//    class AwesomeButtonClick implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            awesomeButtonClicked();
//        }
//    }

    private void startRecording() {

        Log.d("MainActivity", "startRecording");
        if (mSimulation) {
//            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    // Retrieve data from the intent
//                    Location location = intent.getBundleExtra("Location").getParcelable("location");
//                    if (location != null && googleMapsFragment != null) {
//                        googleMapsFragment.addLocation(location);
//                    }
//                }
//            }, new IntentFilter("GPSSimulatorLocation"));
//
//            mGpsSimulator.startSimulation(getApplicationContext());
        } else {
            Log.d("MainActivity", "startRecording");
            mGpsService = new GpsService(new LocationListener() {
                @Override
                public void onLocationReceived(Location location) {
                    Log.d("MainActivity", "Latitude " + location.getLatitude() + ", longitude " + location.getLongitude());

                    TrackPoint point = new TrackPoint(location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getTime());
                    DatabaseHelper.getInstance().createCoordinate(point);

                    if (location != null && googleMapsFragment != null) {
                        googleMapsFragment.drawPoint(point);
                    }
                }
            });
        }
    }

    private void stopRecording() {
        if (mSimulation) {
            Toast.makeText(MainActivity.this, "--->", Toast.LENGTH_SHORT).show();
            mGpsSimulator.stopSimulation();
        } else {
            stopService(new Intent(getBaseContext(), GpsService.class));
        }
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

    private boolean isReadPhoneNumberPermissionGranted() {
        return ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadPhoneNumberPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_NUMBERS)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS}, PHONE_NUMBERS_PERMISSION_CODE);
    }

    private boolean isReadPhoneStatePermissionGranted() {
        return ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_CODE);
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
            case PHONE_NUMBERS_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "PHONE_NUMBERS_PERMISSION_CODE granted");
                    // Permission granted
                } else {
                    Log.d("MainActivity", "PHONE_NUMBERS_PERMISSION_CODE denied");
                    // Permission denied
                }
                return;
            }
            case PHONE_STATE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Log.d("MainActivity", "PHONE_STATE_PERMISSION_CODE granted");
                } else {
                    // Permission denied
                    Log.d("MainActivity", "PHONE_STATE_PERMISSION_CODE denied");
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void registerSmsBroadcastReceiver()
    {
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        smsBroadcastReceiver.setSmsListener(new SmsListener() {
            @Override
            public void onSmsReceived(String sender, String text) {
                Log.d("MainActivity", "sender is " + sender + ", message is " + text);
                if(text.equals("enable")) {
                    startRecording();
                } else if(text.equals("disable")) {
                    stopRecording();
                } else {
                    Log.d("MainActivity", "Message text ignored");
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}