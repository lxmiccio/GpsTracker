package com.gpstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapsFragment extends GoogleMapsFragment implements OnMapReadyCallback {

    public final static String TAG = "MapsFragment";
    private static MapsFragment mInstance = null;

    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mSpeedText;

    private FloatingActionButton mStartRecording;
    private FloatingActionButton mStopRecording;

    public MapsFragment() {
        super();
    }

    public static MapsFragment getInstance() {
        if (mInstance == null) {
            mInstance = new MapsFragment();
        }
        return mInstance;
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
        return inflater.inflate(R.layout.fragment_google_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLatitudeText = view.findViewById(R.id.latitude);
        mLongitudeText = view.findViewById(R.id.longitude);
        mSpeedText = view.findViewById(R.id.speed);
        mChronometer = view.findViewById(R.id.chronometer);

        mStartRecording = view.findViewById(R.id.start_recording);
        mStartRecording.setOnClickListener(mStartRecordingClickListener);

        mStopRecording = view.findViewById(R.id.stop_recording);
        mStopRecording.setOnClickListener(mStopRecordingClickListener);
        mStopRecording.hide();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        clear();
        drawSessions(mDb.getAllSessions());
    }

    public void refresh() {
        if (mMap != null) {
            clear();
            drawSessions(mDb.getAllSessions());
        }
    }

    private View.OnClickListener mStartRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Use the Builder class for convenient dialog construction
            final EditText txtTrackName = new EditText(MainActivity.getContext());
            txtTrackName.setHint("Track");

            new AlertDialog.Builder(MainActivity.getContext())
                    .setMessage(R.string.track_name_message)
                    .setView(txtTrackName)
                    .setPositiveButton(R.string.track_name_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Delete the route on the map
                            clear();

                            // Show stop recording button
                            mStartRecording.hide();
                            mStopRecording.show();

                            GpsService gpsService = GpsService.getInstance();
                            gpsService.createTrack(txtTrackName.getText().toString());

                            gpsService.setGpsListener(mGpsListener);

                            // start location updates
                            gpsService.startLocationUpdates();

                            // start session timer
                            mStartingTime = System.currentTimeMillis();
                            mTimerHandler.removeCallbacks(mTimerTask);
                            mTimerHandler.postDelayed(mTimerTask, 1000);
                        }
                    })
                    .setNegativeButton(R.string.track_name_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Delete the route on the map
                            clear();
                        }
                    })
                    .show();
        }
    };

    private View.OnClickListener mStopRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mLatitudeText.setVisibility(View.INVISIBLE);
            mLongitudeText.setVisibility(View.INVISIBLE);
            mSpeedText.setVisibility(View.INVISIBLE);
            mChronometer.setVisibility(View.INVISIBLE);

            mStopRecording.hide();
            mStartRecording.show();

            // stop location updates
            GpsService.getInstance().stopLocationUpdates();

            // stop session timer
            mTimerHandler.removeCallbacks(mTimerTask);

            refresh();
        }
    };

    private GpsListener mGpsListener = new GpsListener() {
        @Override
        public void onLocationReceived(TrackPoint trackPoint) {
            mLatitudeText.setText("Latitudine: " + String.valueOf(trackPoint.getLatitude()));
            mLongitudeText.setText("Longitudine: " + String.valueOf(trackPoint.getLongitude()));
            mSpeedText.setText("Velocità: " + String.valueOf(Double.valueOf(trackPoint.getSpeed()).intValue()) + " km/h");

            if (mLatitudeText.getVisibility() == View.INVISIBLE) {
                mLatitudeText.setVisibility(View.VISIBLE);
                mLongitudeText.setVisibility(View.VISIBLE);
                mSpeedText.setVisibility(View.VISIBLE);
            }

            drawPoint(trackPoint);
        }
    };
}