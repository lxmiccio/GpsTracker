package com.gpstracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class TrackFragment extends Fragment implements OnMapReadyCallback {

    private static TrackFragment mInstance = null;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private Polyline mRoute;
    private PolylineOptions mRouteOptions;
    private ArrayList<LatLng> mLatLngs;

    private Polyline mGhostRoute;
    private PolylineOptions mGhostRouteOptions;
    private ArrayList<LatLng> mGhostLatLngs;

    private Track mTrack;

    private FloatingActionButton mStartRacing;
    private FloatingActionButton mStopRacing;

    public TrackFragment() {
        mLatLngs = new ArrayList<>();
        mGhostLatLngs = new ArrayList<>();
    }

    public static TrackFragment getInstance() {
        if (mInstance == null) {
            mInstance = new TrackFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mStartRacing = view.findViewById(R.id.start_racing);
        mStartRacing.setOnClickListener(mStartRecordingClickListener);

        mStopRacing = view.findViewById(R.id.stop_racing);
        mStopRacing.setOnClickListener(mStopRecordingClickListener);
        mStopRacing.hide();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setZoomControls(true);

        clear();
        if (mTrack != null) {
            drawTrack(mTrack);
        } else {
            Log.w("TrackFragment", "Track is null");
        }

        setMapType(MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getString("MapType", "Normal"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    public void drawTrack(Track track) {
        Log.d("TrackFragment", "Drawing track " + track.toString());

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.visible(true);
        polylineOptions.width(8);

        ArrayList<TrackPoint> points = track.getPoints();
        for (int i = 0; i < points.size(); ++i) {
            LatLng latLng = new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude());
            polylineOptions.add(latLng);

            if (i == 0) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker at beginning"));
            } else if (i == points.size() - 1) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker at end"));
            } else {
                // Draw marker at current point
                Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_coordinate);
                circleDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                circleDrawable.setBounds(0, 0, 30, 30);
                circleDrawable.draw(canvas);

                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng));
            }
        }

        mMap.addPolyline(polylineOptions);

        if (points.size() > 0) {
            centerCamera(points.get(0));
        }
    }

    public void drawPoint(TrackPoint point) {

        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

        boolean ignore = false;

        if (mLatLngs.size() == 0) {
            mLatLngs.add(latLng);

            // Draw new location
            mRouteOptions = new PolylineOptions();
            mRouteOptions.color(Color.BLACK);
            mRouteOptions.visible(true);
            mRouteOptions.width(8);

            mRouteOptions.add(latLng);
            mRoute = mMap.addPolyline(mRouteOptions);
        } else {
            // Compute distance between previous location
            Location currentLocation = new Location("");
            currentLocation.setLatitude(point.getLatitude());
            currentLocation.setLongitude(point.getLongitude());

            Location previousLocation = new Location("");
            previousLocation.setLatitude(mLatLngs.get(mLatLngs.size() - 1).latitude);
            previousLocation.setLongitude(mLatLngs.get(mLatLngs.size() - 1).latitude);

            double distance = currentLocation.distanceTo(previousLocation);
//            ignore = shouldIgnoreLocationChange(currentLocation, previousLocation);

            // Add point only if distance is greater that MINIMUM_LOCATIONS_DISTANCE
            if (distance >= 0/* && !ignore*/) {
                mLatLngs.add(latLng);
            }
        }

//        if (!ignore) {
        // Draw marker at current point
        Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_coordinate);
        circleDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        circleDrawable.setBounds(0, 0, 30, 30);
        circleDrawable.draw(canvas);

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng));

        // Move camera to user location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        } else {
//            Log.d("GoogleMaps", "Location ignored");
//        }
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

    public void centerCamera(TrackPoint point) {
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void setZoomControls(boolean status) {
        mMap.getUiSettings().setZoomControlsEnabled(status);
    }

    public void setMapType(String type) {
        switch (type) {
            case "Normal":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Terrain":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "Hybrid":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    public void clear() {
        mMap.clear();
    }

    private View.OnClickListener mStartRecordingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Show stop recording button
            mStartRacing.hide();
            mStopRacing.show();

            GpsService gpsService = GpsService.getInstance();
            gpsService.setTrack(mTrack);
            //gpsService.createSession(mTrack.getName());

            gpsService.setGpsListener(new GpsListener() {
                @Override
                public void onLocationReceived(TrackPoint trackPoint) {
                    drawPoint(trackPoint);

                    TrackPoint point = mTrack.getClosestTrackPoint(trackPoint.getTime());
                    Log.d("TrackFragment", "Closest point is " + point.toString());
                    drawGhostPoint(point);
                }
            });

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
}