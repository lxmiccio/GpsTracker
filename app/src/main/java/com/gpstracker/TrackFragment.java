package com.gpstracker;

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

public class TrackFragment extends GoogleMapsFragment implements OnMapReadyCallback {

    private static TrackFragment mInstance = null;

    private Polyline mGhostRoute;
    private PolylineOptions mGhostRouteOptions;
    private Marker mGhostMarker;
    private ArrayList<LatLng> mGhostLatLngs;

    private Session mReferenceSession;

    private TrackPoint mPreviousPoint;
    private int mTraveledDistance;

    private TextView mYou;
    private TextView mDistance;
    private TextView mGhost;
    private TextView mGhostDistance;
    private TextView mDifference;

    private FloatingActionButton mStartRacing;
    private FloatingActionButton mStopRacing;

    public TrackFragment() {
        super();
        mGhostLatLngs = new ArrayList<>();
        mPreviousPoint = null;
        mTraveledDistance = 0;
    }

    public static TrackFragment getInstance() {
        if (mInstance == null) {
            mInstance = new TrackFragment();
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
        return inflater.inflate(R.layout.fragment_track, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mYou = view.findViewById(R.id.you);
        mDistance = view.findViewById(R.id.distance);

        mGhost = view.findViewById(R.id.ghost);
        mGhostDistance = view.findViewById(R.id.ghost_distance);

        mDifference = view.findViewById(R.id.difference);
        mChronometer = view.findViewById(R.id.chronometer);

        mStartRacing = view.findViewById(R.id.start_racing);
        mStartRacing.setOnClickListener(mStartRecordingClickListener);

        mStopRacing = view.findViewById(R.id.stop_racing);
        mStopRacing.setOnClickListener(mStopRecordingClickListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        clear();
        if (mReferenceSession != null) {
            drawSession(mReferenceSession);
        } else {
            Log.w("TrackFragment", "Track is null");
        }
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

    private View.OnClickListener mStartRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Show stop recording button
            mStartRacing.hide();
            mStopRacing.show();

            // start session timer
            mStartingTime = System.currentTimeMillis();
            mTimerHandler.removeCallbacks(mTimerTask);
            mTimerHandler.postDelayed(mTimerTask, 1000);

//            ??????????
//            GpsService gpsService = GpsService.getInstance();
//            gpsService.setTrack(mTrack);
//            gpsService.createSession(mTrack.getName());

            GpsService gpsService = GpsService.getInstance();
            gpsService.setGpsListener(mGpsListener);

            gpsService.startLocationUpdates();
        }
    };

    private View.OnClickListener mStopRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mStopRacing.hide();
            mStartRacing.show();
            GpsService.getInstance().stopLocationUpdates();
        }
    };

    private GpsListener mGpsListener = new GpsListener() {
        @Override
        public void onLocationReceived(TrackPoint trackPoint) {
            drawPoint(trackPoint);

            TrackPoint closestPoint = mReferenceSession.getClosestTrackPoint(trackPoint.getTime());
            Log.d("TrackFragment", "Closest point is " + closestPoint.toString());
            drawGhostPoint(closestPoint);

            int ghostTraveledDistance = mReferenceSession.getTraveledDistance(trackPoint.getTime());
            mGhostDistance.setText(String.valueOf(ghostTraveledDistance) + " m");

            if (mPreviousPoint != null) {
                mTraveledDistance += trackPoint.distanceTo(mPreviousPoint);
            } else {
                mTraveledDistance = 0;
            }
            mPreviousPoint = trackPoint;

            mDistance.setText(String.valueOf(mTraveledDistance) + " m");

            int difference = mTraveledDistance - ghostTraveledDistance;
            mDifference.setText(String.valueOf(difference) + " m");

            if (mYou.getVisibility() == View.INVISIBLE) {
                mYou.setVisibility(View.VISIBLE);
                mGhost.setVisibility(View.VISIBLE);
                mDistance.setVisibility(View.VISIBLE);
                mGhostDistance.setVisibility(View.VISIBLE);
                mDifference.setVisibility(View.VISIBLE);
                mChronometer.setVisibility(View.VISIBLE);
            }
        }
    };
}