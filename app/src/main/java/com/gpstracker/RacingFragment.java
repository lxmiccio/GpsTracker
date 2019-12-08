package com.gpstracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class RacingFragment extends GoogleMapsFragment implements OnMapReadyCallback {

    public final static String TAG = "RacingFragment";
    private static RacingFragment mInstance = null;

    private Polyline mGhostRoute;
    private PolylineOptions mGhostRouteOptions;
    private Marker mGhostMarker;
    private ArrayList<LatLng> mGhostLatLngs;

    private Track mReferenceSessionTrack;
    private Session mReferenceSession;

    private RelativeLayout mInfo;
    private TextView mDistance;
    private TextView mSpeed;
    private TextView mGhostDistance;
    private TextView mGhostSpeed;
    private TextView mDifference;

    private FloatingActionButton mCenterPosition;
    private FloatingActionButton mStartRacing;
    private FloatingActionButton mStopRacing;

    public RacingFragment() {
        super();
    }

    public static RacingFragment getInstance() {
        if (mInstance == null) {
            mInstance = new RacingFragment();
        }
        return mInstance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mGpsService.setGpsListener(mGpsListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mGpsService.isTracking()) {
            // Stop location updates
            mGpsService.stopLocationUpdates();

            // Discard current session
            mGpsService.discardCurrentSession();

            // Stop session timer
            mTimerHandler.removeCallbacks(mTimerTask);
        }

        mGpsService.setGpsListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Obtain the SupportMapFragment
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // Initialize the SupportMapFragment
        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.map, mMapFragment).commit();
        }

        // Get notified when the map is ready to be used
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.racing_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mInfo = view.findViewById(R.id.info);

        mDistance = view.findViewById(R.id.distance_value);
        mSpeed = view.findViewById(R.id.speed_value);

        mGhostDistance = view.findViewById(R.id.ghost_distance_value);
        mGhostSpeed = view.findViewById(R.id.ghost_speed_value);

        mDifference = view.findViewById(R.id.difference_value);
        mChronometer = view.findViewById(R.id.chronometer_value);

        mCenterPosition = view.findViewById(R.id.center_position);
        mCenterPosition.setOnClickListener(mCenterPositionClickListener);

        mStartRacing = view.findViewById(R.id.start_racing);
        mStartRacing.setOnClickListener(mStartRacingClickListener);

        TrackPoint trackPoint = mGpsService.getLatestTrackPoint();
        updateStartRacingButton(trackPoint);

        mStopRacing = view.findViewById(R.id.stop_racing);
        mStopRacing.setOnClickListener(mStopRacingClickListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        resetMap();
    }

    public void resetMap() {
        if (mMap != null) {
            clear();

            // Draw session
            if (mReferenceSession != null) {
                drawSession(mReferenceSession, Color.BLUE);
            } else {
                Log.w("RacingFragment", "Track is null");
            }

            // Draw marker on the user position
            TrackPoint trackPoint = mGpsService.getLatestTrackPoint();
            if (trackPoint != null) {
                drawMarker(trackPoint);
            }
        }
    }

    public void setReferenceSessionTrack(Track referenceSessionTrack) {
        mReferenceSessionTrack = referenceSessionTrack;
    }

    public void setReferenceSession(Session referenceSession) {
        mReferenceSession = referenceSession;
    }

    public void drawGhostPoint(TrackPoint point) {
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

        if (mGhostLatLngs.size() == 0) {
            mGhostLatLngs.add(latLng);

            // Draw new location
            mGhostRouteOptions = new PolylineOptions();
            mGhostRouteOptions.color(Color.RED);
            mGhostRouteOptions.visible(true);
            mGhostRouteOptions.width(8);

            mGhostRouteOptions.add(latLng);
        } else {
            // Compute distance between previous location
            Location currentLocation = new Location("");
            currentLocation.setLatitude(point.getLatitude());
            currentLocation.setLongitude(point.getLongitude());

            Location previousLocation = new Location("");
            previousLocation.setLatitude(mGhostLatLngs.get(mGhostLatLngs.size() - 1).latitude);
            previousLocation.setLongitude(mGhostLatLngs.get(mGhostLatLngs.size() - 1).latitude);

            double distance = currentLocation.distanceTo(previousLocation);
//          boolean ignore = shouldIgnoreLocationChange(currentLocation, previousLocation);

            // Add point only if distance is greater that MINIMUM_LOCATIONS_DISTANCE
            if (distance >= 0/* && !ignore*/) {
                mGhostLatLngs.add(latLng);
            }
        }

        mGhostRoute = mMap.addPolyline(mGhostRouteOptions);

        // Remove previous marker
        if (mGhostMarker != null) {
            mGhostMarker.remove();
            mGhostMarker = null;
        }

        // Draw marker at current point
        Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_coordinate);
        circleDrawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        circleDrawable.setBounds(0, 0, 30, 30);
        circleDrawable.draw(canvas);

        mGhostMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng));
    }

    private void updateStartRacingButton(TrackPoint trackPoint) {
        //Evaluate distance from starting line
        float distanceFromStartingLine = trackPoint.distanceTo(mReferenceSession.getPoints().get(0));
        Log.d("RacingFragment", "Distance from starting line is " + distanceFromStartingLine + " m");

        //Enable StartRacing button only if the user is close to the starting line
        mStartRacing.setEnabled(distanceFromStartingLine <= 10);
        if (distanceFromStartingLine <= 10) {
            mStartRacing.setBackgroundTintList(MainActivity.getContext().getResources().getColorStateList(R.color.colorPrimary));
        } else {
            mStartRacing.setBackgroundTintList(MainActivity.getContext().getResources().getColorStateList(R.color.colorDarkGrey));
        }
    }

    private void detectRaceFinished(TrackPoint trackPoint) {
        // Evaluate distance from finish line
        float distanceFromFinishLine = trackPoint.distanceTo(mReferenceSession.getPoints().get(mReferenceSession.getPoints().size() - 1));
        Log.d("RacingFragment", "Distance from finish line is " + distanceFromFinishLine + " m");

        // Race is finished only if the user is close to the finish line and the traveled distance is similar to the reference one
        if (distanceFromFinishLine <= 10 && Math.abs(mReferenceSession.getLength() - mTraveledDistance) <= 10) {
            mStopRacing.hide();
            mStartRacing.show();

            // Hide information about race
            mInfo.setVisibility(View.INVISIBLE);

            // Stop location updates
            mGpsService.stopLocationUpdates();

            // Save current session
            mGpsService.saveCurrentSession();

            // Stop session timer
            mTimerHandler.removeCallbacks(mTimerTask);
        }
    }

    private View.OnClickListener mStartRacingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Show StopRacing button
            mStartRacing.hide();
            mStopRacing.show();

            // Reset map
            resetMap();

            // Start session timer
            mStartingTime = System.currentTimeMillis();
            mTimerHandler.removeCallbacks(mTimerTask);
            mTimerHandler.postDelayed(mTimerTask, 1000);

            mGpsService.setTrack(mReferenceSessionTrack);

            mGpsService.startLocationUpdates();
        }
    };

    private View.OnClickListener mStopRacingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(MainActivity.getContext())
                    .setMessage(R.string.quit_race_message)
                    .setPositiveButton(R.string.yes_message, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mStopRacing.hide();
                            mStartRacing.show();

                            // Hide information about race
                            mInfo.setVisibility(View.INVISIBLE);

                            // Stop location updates
                            mGpsService.stopLocationUpdates();

                            // Discard current session
                            mGpsService.discardCurrentSession();

                            // Stop session timer
                            mTimerHandler.removeCallbacks(mTimerTask);

                            // Reset the map
                            resetMap();
                        }
                    })
                    .setNegativeButton(R.string.no_message, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Do nothing
                        }
                    })
                    .show();
        }
    };

    private GpsListener mGpsListener = new GpsListener() {
        @Override
        public void onLocationReceived(TrackPoint trackPoint) {
            // Draw marker on the user position
            drawMarker(trackPoint);

            if (mGpsService.isTracking()) {
                drawPoint(trackPoint);
                centerCamera(trackPoint);

                int ghostTraveledDistance = mReferenceSession.getTraveledDistance(trackPoint.getTime());
                mGhostDistance.setText(String.valueOf(ghostTraveledDistance) + " m");

                TrackPoint closestPoint = mReferenceSession.getClosestTrackPoint(trackPoint.getTime());
                if (closestPoint != null) {
                    drawGhostPoint(closestPoint);
                    mGhostSpeed.setText(String.valueOf(Double.valueOf(closestPoint.getSpeed()).intValue()) + " km/h");
                }

                // Update traveled distance
                updateTraveledDistance(trackPoint);

                mDistance.setText(String.valueOf(mTraveledDistance) + " m");
                mSpeed.setText(String.valueOf(Double.valueOf(trackPoint.getSpeed()).intValue()) + " km/h");

                int difference = mTraveledDistance - ghostTraveledDistance;
                mDifference.setText(String.valueOf(difference) + " m");
                mDifference.setTextColor(difference >= 0 ? Color.GREEN : Color.RED);

                if (mInfo.getVisibility() == View.INVISIBLE) {
                    mInfo.setVisibility(View.VISIBLE);
                }

                // Check if user reached the finish line
                detectRaceFinished(trackPoint);
            } else {
                // Update StartRacing button
                updateStartRacingButton(trackPoint);
            }
        }
    };
}