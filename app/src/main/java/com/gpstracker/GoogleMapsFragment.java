package com.gpstracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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

public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback {

    protected Polyline mRoute;
    protected PolylineOptions mRouteOptions;
    protected ArrayList<LatLng> mLatLngs;

    protected GoogleMap mMap;
    protected SupportMapFragment mMapFragment;

    protected DatabaseHelper mDb;

    public GoogleMapsFragment() {
        mLatLngs = new ArrayList<>();
        mDb = DatabaseHelper.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16f));

        setMapType(MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getString("MapType", "Normal"));
        setZoomControls(true);
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
                mRouteOptions.add(latLng);
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

    public void drawPoints(ArrayList<TrackPoint> points) {
        for (TrackPoint iPoint : points) {
            drawPoint(iPoint);
        }
    }

    public void drawSession(Session session) {

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.visible(true);
        polylineOptions.width(8);

        ArrayList<TrackPoint> points = session.getPoints();
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
    }

    public void drawSessions(ArrayList<Session> sessions) {
        for (int i = 0; i < sessions.size(); ++i) {
            Session session = sessions.get(i);
            drawSession(session);

            if (i == sessions.size() - 1) {
                ArrayList<TrackPoint> points = session.getPoints();
                if (points.size() > 0) {
                    centerCamera(points.get(points.size() - 1));
                }
            }
        }
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

//    private boolean shouldIgnoreLocationChange(Location oldLocation, Location newLocation) {
//        if (oldLocation == null) {
//            // didn't have any location before, so accept new
//            return false;
//        } else if (null == newLocation || !newLocation.hasAccuracy() || newLocation.getAccuracy() > 150) {
//            // new location got invalid or too vague accuracy so ignore it
//            return true;
//        }
//
//        // ignore change if change is smaller then 3 meters and
//        if (oldLocation.distanceTo(newLocation) < 3f && newLocation.getAccuracy() >= oldLocation.getAccuracy()) {
//            return true;
//        }
//        return false;
//    }
}