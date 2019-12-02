package com.gpstracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
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

    private TrackPoint mLatestTrackPoint;
    private boolean mTracking;
    private Track mTrack;
    private Session mSession;

    private LocationRequest mLowRateLocationRequest; // To update the current position in the map
    private LocationRequest mHighRateLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private Handler mSimulationHandler;
    private ArrayList<TrackPoint> mSimulationPoints;

    private DatabaseHelper mDb;

    public GpsService() {
        mTracking = false;

        mDb = DatabaseHelper.getInstance();

        // Low rate location requests are used to update the current user position on the map
        // Since GPS has a high energy consumption, lowering the updates interval will decrease battery drain
        mLowRateLocationRequest = LocationRequest.create()
                .setInterval(10000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // High rate location requests are used while recording to better track the user movements
        mHighRateLocationRequest = LocationRequest.create()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.getContext());

        //Start low rate location updated
        startLowRateLocationUpdated();
    }

    public static GpsService getInstance() {
        if (mInstance == null) {
            mInstance = new GpsService();
        }
        return mInstance;
    }

    public void createTrack(String name) {
        mTrack = mDb.createTrack(name);
        mSession = mDb.createSession(mTrack);
        mSession.setName(name);
    }

    public void setTrack(Track track) {
        mTrack = track;
        mSession = mDb.createSession(mTrack);
        mSession.setName(mTrack.getName());
    }

    public void startLowRateLocationUpdated() {
        if (ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted
            mFusedLocationClient.requestLocationUpdates(mLowRateLocationRequest, mLocationCallback, Looper.myLooper());
        } else {
            //Request Location Permission
            Log.d("GpsService", "checkLocationPermission");
            checkLocationPermission();
        }
    }

    public void stopLowRateLocationUpdated() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void startLocationUpdates() {
        if (!SettingsHandler.isGpsSimulationEnabled()) {
            if (mTrack != null) {
                if (ContextCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    //Stop low rate location updates
                    stopLowRateLocationUpdated();

                    //Start high rate location updates
                    mTracking = true;
                    mFusedLocationClient.requestLocationUpdates(mHighRateLocationRequest, mLocationCallback, Looper.myLooper());
                } else {
                    //Request Location Permission
                    Log.d("GpsService", "checkLocationPermission");
                    checkLocationPermission();
                }
            }
        } else {
            //Stop low rate location updates
            stopLowRateLocationUpdated();

            mSimulationHandler = new Handler();

            long sessionId = SettingsHandler.getSessionToSimulate();
            Session session = mDb.getSession(sessionId);
            if (session != null) {
                mSimulationPoints = session.getPoints();

                mTracking = true;
                mSimulationHandler.postDelayed(mSimulationTask, 1000);
            }
        }
    }

    public void stopLocationUpdates() {
        mTracking = false;

        if (mSimulationHandler == null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } else {
            mSimulationHandler.removeCallbacks(mSimulationTask);
            mSimulationHandler = null;
        }

        //Start low rate location updated
        startLowRateLocationUpdated();
    }

    public void saveCurrentSession() {
        mSession.setEndingDate(new Date());
        mDb.updateSession(mSession);

//        if (mSimulationHandler == null) {
        GpxHandler.saveGpx(MainActivity.getContext().getFilesDir(), mSession);
//        }

        mTrack = null;
        mSession = null;
    }

    public void discardCurrentSession() {
        mSession.setEndingDate(new Date());
        mDb.deleteSession(mSession.getId());

        mTrack = null;
        mSession = null;
    }

    public TrackPoint getLatestTrackPoint() {
        return mLatestTrackPoint;
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

                TrackPoint point;

                if (mTracking) {
                    Date startingDate = mSession.getStartingDate();
                    Date currentDate = new Date();
                    long diff =  currentDate.getTime() - startingDate.getTime();

                    point = new TrackPoint(location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed(), diff);

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

                    mDb.createCoordinate(point, mSession.getId());
                } else {
                    point = new TrackPoint(location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed(), 0);
                }

                mLatestTrackPoint = point;

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
                    TrackPoint point = mSimulationPoints.get(0);
                    mSimulationPoints.remove(0);

                    if (mGpsListener != null) {
                        mGpsListener.onLocationReceived(point);
                    }

                    mSession.appendPoint(point);
                    mDb.createCoordinate(point, mSession.getId());

                    if (mSimulationPoints.size() > 0) {
                        TrackPoint nextPoint = mSimulationPoints.get(0);
                        long diffTime = nextPoint.getTime() - point.getTime();
                        mSimulationHandler.postDelayed(mSimulationTask, diffTime);
                    }
                } else {
                    stopLocationUpdates();
                }
            }
        }
    };
}
