package com.gpstracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class GpsService {

    private static GpsService mInstance = null;

    private GoogleMapsFragment mMapsFragment;
    private boolean mTracking;
    private Track mTrack;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseHelper mDb;

    public GpsService() {
        mMapsFragment = GoogleMapsFragment.getInstance();
        mTracking = false;

        mDb = DatabaseHelper.getInstance();

        // Request location updates
        mLocationRequest = LocationRequest.create()
                .setFastestInterval(1000)
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.getContext());
    }

    public static GpsService getInstance() {
        if (mInstance == null) {
            mInstance = new GpsService();
        }
        return mInstance;
    }

    public void createTrack(String name) {
        mTrack = new Track(name);
    }

    public void startLocationUpdates() {
        if (mTrack != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    mTracking = true;
                    long trackId = mDb.createTrack(mTrack.getName());
                    Log.d("GpsService", "Starting recording, trackId is " + trackId);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                } else {
                    //Request Location Permission
                    Log.d("GpsService", "checkLocationPermission");
                    checkLocationPermission();
                }
            } else {
                //Request Location Permission
                mTracking = true;
                long trackId = mDb.createTrack(mTrack.getName());
                Log.d("GpsService", "Starting recording, trackId is " + trackId);
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        }
    }

    public void stopLocationUpdates() {
        mTracking = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        double length = mTrack.getLength();
        mDb.updateTrack(length);
        mMapsFragment.refresh();

        GpxHandler.saveGpx(MainActivity.getContext().getFilesDir(), mTrack);

        mTrack = null;
    }

    public boolean isTracking() {
        return mTracking;
    }

    public Track getTrack() {
        return mTrack;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(MainActivity.getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions((Activity) MainActivity.getContext(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MainActivity.PHONE_ACCESS_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((Activity) MainActivity.getContext(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MainActivity.PHONE_ACCESS_FINE_LOCATION);
            }
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                TrackPoint point = new TrackPoint(location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getTime());
                mTrack.appendPoint(point);

                mDb.createCoordinate(point, mDb.getCurrentTrackId());

                mMapsFragment.drawPoint(point);
            }
        }
    };
}
