package com.gpstracker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TrackFragment extends GoogleMapsFragment implements OnMapReadyCallback {

    private static TrackFragment mInstance = null;

    private Polyline mGhostRoute;
    private PolylineOptions mGhostRouteOptions;
    private ArrayList<LatLng> mGhostLatLngs;

    private Session mSession;

    private TextView mGhostDistance;
    private TextView mDistance;
    private TextView mDifference;

    private FloatingActionButton mStartRacing;
    private FloatingActionButton mStopRacing;

    public TrackFragment() {
        super();
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

        mGhostDistance = view.findViewById(R.id.ghost_distance);
        mGhostDistance.setVisibility(View.INVISIBLE);

        mDistance = view.findViewById(R.id.distance);
        mDistance.setVisibility(View.INVISIBLE);

        mDifference = view.findViewById(R.id.difference);
        mDistance.setVisibility(View.INVISIBLE);

        mStartRacing = view.findViewById(R.id.start_racing);
        mStartRacing.setOnClickListener(mStartRecordingClickListener);

        mStopRacing = view.findViewById(R.id.stop_racing);
        mStopRacing.setOnClickListener(mStopRecordingClickListener);
        mStopRacing.hide();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        clear();
        if (mSession != null) {
            drawSession(mSession);
        } else {
            Log.w("TrackFragment", "Track is null");
        }
    }

    public void setSession(Session session) {
        mSession = session;
    }

    public void drawGhostPoint(TrackPoint point) {

        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

        boolean ignore = false;

        if (mGhostLatLngs.size() == 0) {
            mGhostLatLngs.add(latLng);

            // Draw new location
            mGhostRouteOptions = new PolylineOptions();
            mGhostRouteOptions.color(Color.RED);
            mGhostRouteOptions.visible(true);
            mGhostRouteOptions.width(8);

            mGhostRouteOptions.add(latLng);
            mGhostRoute = mMap.addPolyline(mGhostRouteOptions);
        } else {
            // Compute distance between previous location
            Location currentLocation = new Location("");
            currentLocation.setLatitude(point.getLatitude());
            currentLocation.setLongitude(point.getLongitude());

            Location previousLocation = new Location("");
            previousLocation.setLatitude(mGhostLatLngs.get(mGhostLatLngs.size() - 1).latitude);
            previousLocation.setLongitude(mGhostLatLngs.get(mGhostLatLngs.size() - 1).latitude);

            double distance = currentLocation.distanceTo(previousLocation);
//            ignore = shouldIgnoreLocationChange(currentLocation, previousLocation);

            // Add point only if distance is greater that MINIMUM_LOCATIONS_DISTANCE
            if (distance >= 0/* && !ignore*/) {
                mGhostLatLngs.add(latLng);
            }
        }

//        if (!ignore) {
        // Draw marker at current point
        Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_coordinate);
        circleDrawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        circleDrawable.setBounds(0, 0, 30, 30);
        circleDrawable.draw(canvas);

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng));

//        } else {
//            Log.d("GoogleMaps", "Location ignored");
//        }
    }

    private View.OnClickListener mStartRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Show stop recording button
            mStartRacing.hide();
            mStopRacing.show();

//            ??????????
//            GpsService gpsService = GpsService.getInstance();
//            gpsService.setTrack(mTrack);
//            gpsService.createSession(mTrack.getName());

            GpsService gpsService = GpsService.getInstance();
            gpsService.setGpsListener(new GpsListener() {
                @Override
                public void onLocationReceived(TrackPoint trackPoint) {
                    drawPoint(trackPoint);

                    TrackPoint closestPoint = mSession.getClosestTrackPoint(trackPoint.getTime());
                    Log.d("TrackFragment", "Closest point is " + closestPoint.toString());
                    drawGhostPoint(closestPoint);

                    int ghostTraveledDistance = mSession.getTraveledDistance(trackPoint.getTime());
                    mGhostDistance.setText(String.valueOf(ghostTraveledDistance) + " m");
                    mGhostDistance.setVisibility(View.VISIBLE);

                    int traveledDistance = mSession.getTraveledDistance(trackPoint.getTime());
                    mDistance.setText(String.valueOf(traveledDistance) + " m");
                    mDistance.setVisibility(View.VISIBLE);

                    int difference = traveledDistance - ghostTraveledDistance;
                    mDifference.setText(String.valueOf(difference) + " m");
                    mDifference.setVisibility(View.VISIBLE);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TrackFragment", "Timer timed out");
                            mGhostDistance.setVisibility(View.INVISIBLE);
                            mDistance.setVisibility(View.INVISIBLE);
                            mDifference.setVisibility(View.INVISIBLE);
                        }
                    }, 2500);  //the time is in milliseconds
                }
            });

            gpsService.startLocationUpdates();
            // handler.removeCallbacksAndMessages(null); To stop
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
}