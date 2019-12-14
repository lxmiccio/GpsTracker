package com.smarttracker.view.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.smarttracker.R;
import com.smarttracker.model.TrackPoint;
import com.smarttracker.services.GpsListener;
import com.smarttracker.view.activities.MainActivity;

public class RecordingFragment extends GoogleMapsFragment implements OnMapReadyCallback {

    public final static String TAG = "RecordingFragment";
    private static RecordingFragment mInstance = null;

    private boolean mCenterMapToUserPosition;

    private RelativeLayout mInfo;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mDistanceText;
    private TextView mSpeedText;

    private FloatingActionButton mCenterPosition;
    private FloatingActionButton mStartRecording;
    private FloatingActionButton mStopRecording;

    public RecordingFragment() {
        super();
        mCenterMapToUserPosition = true;
    }

    public static RecordingFragment getInstance() {
        if (mInstance == null) {
            mInstance = new RecordingFragment();
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

        // Discard Session if a Recording is in progress
        if (mGpsService.isTracking()) {
            finishRecording(false);
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
        return inflater.inflate(R.layout.recording_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mInfo = view.findViewById(R.id.info);

        mLatitudeText = view.findViewById(R.id.latitude_value);
        mLongitudeText = view.findViewById(R.id.longitude_value);
        mDistanceText = view.findViewById(R.id.distance_value);
        mSpeedText = view.findViewById(R.id.speed_value);
        mChronometer = view.findViewById(R.id.chronometer_value);

        mCenterPosition = view.findViewById(R.id.center_position);
        mCenterPosition.setOnClickListener(mCenterPositionClickListener);

        mStartRecording = view.findViewById(R.id.start_recording);
        mStartRecording.setOnClickListener(mStartRecordingClickListener);

        mStopRecording = view.findViewById(R.id.stop_recording);
        mStopRecording.setOnClickListener(mStopRecordingClickListener);
        mStopRecording.hide();

        mGpsService.setGpsListener(mGpsListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        resetMap();
    }

    @Override
    public void resetMap() {
        super.resetMap();
        if (mMap != null) {
            // Draw all sessions
            drawSessions(mDb.getAllSessions());
        }
    }

    private void finishRecording(boolean save) {
        mStopRecording.hide();
        mStartRecording.show();

        // Hide information about race
        mInfo.setVisibility(View.INVISIBLE);

        // Stop location updates
        mGpsService.stopLocationUpdates();

        // Stop timer
        mTimerHandler.removeCallbacks(mTimerTask);

        if (save) {
            // Save current session
            mGpsService.saveCurrentSession();
        } else {
            // Discard current session
            mGpsService.discardCurrentSession();
        }

        // Reset the map
        resetMap();
    }

    private View.OnClickListener mStartRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Use the Builder class for convenient dialog construction
            final EditText txtTrackName = new EditText(MainActivity.getContext());
            txtTrackName.setHint(R.string.track_name_hint);

            new AlertDialog.Builder(MainActivity.getContext())
                    .setMessage(R.string.track_name_message)
                    .setView(txtTrackName)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Reset the map
                            resetMap();

                            // Show stop recording button
                            mStartRecording.hide();
                            mStopRecording.show();

                            // Reset the previous point
                            mPreviousTrackPoint = null;

                            // Create a new track
                            mGpsService.createTrack(txtTrackName.getText().toString());

                            // Start location updates
                            mGpsService.startLocationUpdates();

                            // Start session timer
                            mStartingTime = System.currentTimeMillis();
                            mTimerHandler.removeCallbacks(mTimerTask);
                            mTimerHandler.postDelayed(mTimerTask, 1000);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Reset the map
                            resetMap();
                        }
                    })
                    .show();
        }
    };

    private View.OnClickListener mStopRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mGpsService.getSession().getLength() >= 10) {
                // Save current session
                finishRecording(true);
            } else {
                // Discard current session if shorten than 10 m
                new AlertDialog.Builder(MainActivity.getContext())
                        .setTitle(R.string.discard_track)
                        .setMessage(R.string.track_minimum_length)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishRecording(false);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Do nothing
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    };

    private GpsListener mGpsListener = new GpsListener() {
        @Override
        public void onLocationReceived(TrackPoint trackPoint) {
            if (mGpsService.isTracking()) {
                // Update track and user position
                drawPoint(trackPoint);
                centerCamera(trackPoint);

                // Update latitude and longitude
                mLatitudeText.setText(String.valueOf(trackPoint.getLatitude()));
                mLongitudeText.setText(String.valueOf(trackPoint.getLongitude()));

                // Update traveled distance and speed
                updateTraveledDistance(trackPoint);
                mDistanceText.setText(String.valueOf(mTraveledDistance) + " m");
                mSpeedText.setText(String.valueOf(Double.valueOf(trackPoint.getSpeed()).intValue()) + " " + getString(R.string.kilometers_per_hour));

                // Show information about race
                if (mInfo.getVisibility() == View.INVISIBLE) {
                    mInfo.setVisibility(View.VISIBLE);
                }
            } else {
                // The first time a TrackPoint is received, center the camera to the User position
                if (mCenterMapToUserPosition) {
                    mCenterMapToUserPosition = false;
                    centerCamera(trackPoint);
                }

                // Update user position
                drawMarker(trackPoint);
            }
        }
    };
}