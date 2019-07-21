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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

interface GpsListener {
    void onLocationReceived(TrackPoint trackPoint);
}

public class GpsService {

    private static GpsService mInstance = null;

    private GpsListener mGpsListener;

    private boolean mTracking;
    private Track mTrack;
    private Session mSession;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseHelper mDb;

    public GpsService() {
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

    public void setTrack(Track track) {
        mTrack = track;
    }

    public void createSession(String name) {
        mSession = new Session();
    }

    public void startLocationUpdates() {
        if (mTrack != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    mTracking = true;
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    initSession();
                } else {
                    //Request Location Permission
                    Log.d("GpsService", "checkLocationPermission");
                    checkLocationPermission();
                }
            } else {
                //Request Location Permission
                mTracking = true;
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                initSession();
            }
        }
    }

    public void stopLocationUpdates() {
        mTracking = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        double length = mSession.getLength();
        mDb.createTrack(mTrack.getName());
        mDb.updateSession(length);

//        GpxHandler.saveGpx(MainActivity.getContext().getFilesDir(), mTrack);

        mTrack = null;
        mSession = null;
    }

    private void initSession() {
        mSession = new Session();
    }

    public boolean isTracking() {
        return mTracking;
    }

    public Track getTrack() {
        return mTrack;
    }

    public Session getSession() {
        return mSession;
    }

    public void setGpsListener(GpsListener listener) {
        mGpsListener = listener;
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

//                Date startingDate = mTrack.getStartingDate();
//                Date currentDate = new Date();
//                long diff = currentDate.getTime() - startingDate.getTime();
//                int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
//                int hours = (int) (diff / (1000 * 60 * 60));
//                int minutes = (int) (diff / (1000 * 60));
//                int seconds = (int) (diff / (1000));

                Date startingDate = mSession.getStartingDate();
                Date currentDate = new Date();
                long diff =  currentDate.getTime() - startingDate.getTime();

                TrackPoint point = new TrackPoint(location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed(), diff);


                float speed = 0;
                ArrayList<TrackPoint> points = mSession.getPoints();
                if(points.size() > 0) {
                    TrackPoint previousPoint = points.get(points.size() - 1);
                    float distance = point.distanceTo(previousPoint);
                    float elapsedTime = point.getTime() - previousPoint.getTime();
                    speed =  distance / (elapsedTime / 1000);
                    // Convert speed from m/s to km/h
                    speed *= 3.6;
                    point.setSpeed(speed);
                }

                mSession.appendPoint(point);

                mDb.createCoordinate(point, mDb.getCurrentTrackId());

                if (mGpsListener != null) {
                    mGpsListener.onLocationReceived(point);
                }
            }
        }
    };
}
