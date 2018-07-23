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

public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback {

    public final static String TAG = "GoogleMapsFragment";
    private static GoogleMapsFragment mInstance = null;

    private OnFragmentInteractionListener mListener;

    private Polyline mRoute;
    private PolylineOptions mRouteOptions;
    private ArrayList<LatLng> mLatLngs;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    public GoogleMapsFragment() {
        mLatLngs = new ArrayList<>();
    }

    public static GoogleMapsFragment getInstance() {
        if (mInstance == null) {
            mInstance = new GoogleMapsFragment();
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
        return inflater.inflate(R.layout.fragment_google_maps, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setZoomControls(true);

        DatabaseHelper db = DatabaseHelper.getInstance();
        drawPoints(db.getAllCoordinates());

        setMapType(MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getString("MapType", "Normal"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(4.5f));
    }

    public void refresh() {
        if (mMap != null) {
            clear();

            DatabaseHelper db = DatabaseHelper.getInstance();
            drawPoints(db.getAllCoordinates());
        }
    }

    public void drawPoint(TrackPoint point) {

        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

        if (mLatLngs.size() == 0) {
            mLatLngs.add(latLng);

            // Draw new location
            mRouteOptions = new PolylineOptions();
            mRouteOptions.color(Color.BLACK);
            mRouteOptions.visible(true);
            mRouteOptions.width(8);

            mRouteOptions.add(latLng);
            mRoute = mMap.addPolyline(mRouteOptions);

            // Draw marker at beginning
            Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_coordinate);
            circleDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            circleDrawable.setBounds(0, 0, 30, 30);
            circleDrawable.draw(canvas);

            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng));
        } else {
            // Compute distance between previous location
            Location currentLocation = new Location("");
            currentLocation.setLatitude(point.getLatitude());
            currentLocation.setLongitude(point.getLongitude());

            Location previousLocation = new Location("");
            previousLocation.setLatitude(mLatLngs.get(mLatLngs.size() - 1).latitude);
            previousLocation.setLongitude(mLatLngs.get(mLatLngs.size() - 1).latitude);

            double distance = currentLocation.distanceTo(previousLocation);

            // Add point only if distance is greater that MINIMUM_LOCATIONS_DISTANCE
            if (distance >= 0) {
                mLatLngs.add(latLng);
            }
        }

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
    }

    public void drawPoints(ArrayList<TrackPoint> points) {
        for (TrackPoint iPoint : points) {
            drawPoint(iPoint);
        }
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

    public interface OnFragmentInteractionListener {
    }
}