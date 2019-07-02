package com.gpstracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class TrackFragment extends Fragment implements OnMapReadyCallback {

    private static TrackFragment mInstance = null;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private Track mTrack;

    public TrackFragment() {
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
}