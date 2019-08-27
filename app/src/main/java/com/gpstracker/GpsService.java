package com.gpstracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
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

    private Handler mSimulationHandler;
    private ArrayList<TrackPoint> mSimulationPoints;

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
        mTrack = mDb.createTrack(name);
        mSession = mDb.createSession(mTrack.getId());
        mSession.setName(name);
    }

    public void setTrack(Track track) {
        mTrack = track;
        mSession = mDb.createSession(mTrack.getId());
        mSession.setName(mTrack.getName());
    }

    public void startLocationUpdates() {
        if (!SettingsHandler.isGpsSimulationEnabled()) {
            if (mTrack != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Location Permission already granted
                        mTracking = true;
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    } else {
                        //Request Location Permission
                        Log.d("GpsService", "checkLocationPermission");
                        checkLocationPermission();
                    }
                } else {
                    //Request Location Permission
                    mTracking = true;
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
            }
        } else {
            mSimulationHandler = new Handler();

            Gpx gpx = GpxHandler.loadGpx(MainActivity.getContext().getFilesDir(), "Prova13");
            mSimulationPoints = gpx.getSession().getPoints();

            mTracking = true;
            mSimulationHandler.postDelayed(mSimulationTask, 1000);
        }
    }

    public void stopLocationUpdates() {
        if (mSimulationHandler == null) {
            mTracking = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            double length = mSession.getLength();
            mDb.updateSession(length);

            GpxHandler.saveGpx(MainActivity.getContext().getFilesDir(), mSession);

            mTrack = null;
            mSession = null;
        } else {
            mTracking = false;
            mSimulationHandler.removeCallbacks(mSimulationTask);
            mSimulationHandler = null;
        }
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

                mDb.createCoordinate(point, mDb.getCurrentSessionId());

                if (mGpsListener != null) {
                    mGpsListener.onLocationReceived(point);
                }
            }
        }
    };

    private Runnable mSimulationTask = new Runnable() {
        public void run() {
            if (mGpsListener != null) {
                if (mSimulationPoints.size() > 0) {
                    TrackPoint point = mSimulationPoints.get(0);;
                    mSimulationPoints.remove(0);

                    if (mGpsListener != null) {
                        mGpsListener.onLocationReceived(point);
                    }

                    mSimulationHandler.postDelayed(mSimulationTask, 1000);
                } else {
                    stopLocationUpdates();
                }
            }
        }
    };
}
