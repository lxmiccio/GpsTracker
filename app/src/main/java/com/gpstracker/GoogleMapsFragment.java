package com.gpstracker;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;

    private ArrayList<Location> mLocations;
    private ArrayList<LatLng> mLatLngs;
    private Polyline mRoute;
    private PolylineOptions mRouteOptions;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private Gpx mCurrentGpx;

    private static final double MINIMUM_LOCATIONS_DISTANCE = 0;

    public GoogleMapsFragment() {
        mLocations = new ArrayList<>();
        mLatLngs = new ArrayList<>();
    }

    public static GoogleMapsFragment newInstance() {
        GoogleMapsFragment fragment = new GoogleMapsFragment();
        return fragment;
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
    }

    public void mStartRecording(String name)
    {
        mCurrentGpx = new Gpx(name);
    }

    public void mStopRecording(boolean save)
    {
        if(save) {

        }
    }

    public void addLocation(Location location) {
        if (location != null) {
            LatLng lLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (mLocations.size() == 0) {
                // Add new location
                mLocations.add(location);

                // Draw new location
                mRouteOptions = new PolylineOptions();
                mRouteOptions.color(Color.BLACK);
                mRouteOptions.visible(true);
                mRouteOptions.width(8);

                mRouteOptions.add(lLatLng);
                mRoute = mMap.addPolyline(mRouteOptions);

                // Draw marker at beginning
                mMap.addMarker(new MarkerOptions().position(lLatLng).title("Marker at beginning"));
            } else {
                // Compute distance between previous location
                double distance = location.distanceTo(mLocations.get(mLocations.size() - 1));

                // Add point only if distance is greater that MINIMUM_LOCATIONS_DISTANCE
                if (distance >= MINIMUM_LOCATIONS_DISTANCE) {
                    mLocations.add(location);

                    ArrayList<LatLng> lLatLngs = new ArrayList<>();
                    for (Location lLocation : mLocations) {
                        lLatLngs.add(new LatLng(lLocation.getLatitude(), lLocation.getLongitude()));
                    }

                    mRoute.setPoints(lLatLngs);
                }
            }

            // Move camera to user location
            // mMap.moveCamera(CameraUpdateFactory.newLatLng(lLatLng));
        }
    }

    public void loadGpx(Gpx gpx) {
        if (gpx != null) {
            ArrayList<TrackPoint> trackPoints = gpx.getTrack().getTrackSegment().getTrackPoints();

            for (TrackPoint iPoint : trackPoints) {
                Log.d("LOAD", iPoint.getLatitude() + " " + iPoint.getLongitude());
                LatLng lLatLng = new LatLng(iPoint.getLatitude(), iPoint.getLongitude());
                mLatLngs.add(lLatLng);

                if (mLatLngs.size() == 1) {
                    // Draw new location
                    mRouteOptions = new PolylineOptions();
                    mRouteOptions.color(Color.BLACK);
                    mRouteOptions.visible(true);
                    mRouteOptions.width(8);

                    mRouteOptions.add(lLatLng);
                    mRoute = mMap.addPolyline(mRouteOptions);

                    // Draw marker at beginning
                    mMap.addMarker(new MarkerOptions().position(lLatLng).title("Marker at beginning"));
                } else {
                    // Compute distance between previous location
                    Location currentLocation = new Location("");
                    currentLocation.setLatitude(iPoint.getLatitude());
                    currentLocation.setLongitude(iPoint.getLongitude());

                    Location previousLocation = new Location("");
                    previousLocation.setLatitude(trackPoints.get(trackPoints.indexOf(iPoint) - 1).getLatitude());
                    previousLocation.setLongitude(trackPoints.get(trackPoints.indexOf(iPoint) - 1).getLongitude());

                    double distance = currentLocation.distanceTo(previousLocation);

                    // Add point only if distance is greater that MINIMUM_LOCATIONS_DISTANCE
                    if (distance >= MINIMUM_LOCATIONS_DISTANCE) {
                        mRoute.setPoints(mLatLngs);
                    }

                    if(trackPoints.indexOf(iPoint) == trackPoints.size() - 1) {
                        mMap.addMarker(new MarkerOptions().position(lLatLng).title("Marker at ending"));
                    }
                }

                // Move camera to user location
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLngs.get(mLatLngs.size() - 1)));
            }
        }
    }

    public void setZoomControls(boolean status) {
        mMap.getUiSettings().setZoomControlsEnabled(status);
    }

    public void setMapType(String type) {
        switch(type) {
            case "Normal":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Terrain":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
    }

    public void clear() {
        mLocations.clear();
        mMap.clear();
    }

    public ArrayList<Location> getLocations() {
        return mLocations;
    }

    public interface OnFragmentInteractionListener {
        // void onFragmentInteraction(Uri uri);
    }
}