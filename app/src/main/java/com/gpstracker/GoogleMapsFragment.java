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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

    private DatabaseHelper mDb;
    private FloatingActionButton mStartRecording;
    private FloatingActionButton mStopRecording;

    public GoogleMapsFragment() {
        mLatLngs = new ArrayList<>();

        mDb = DatabaseHelper.getInstance();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mStartRecording = view.findViewById(R.id.start_recording);
        mStartRecording.setOnClickListener(mStartRecordingClickListener);

        mStopRecording = view.findViewById(R.id.stop_recording);
        mStopRecording.setOnClickListener(mStopRecordingClickListener);
        mStopRecording.hide();
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

        drawTracks(mDb.getAllTracks());

        setMapType(MainActivity.getContext().getSharedPreferences("settings", MODE_PRIVATE).getString("MapType", "Normal"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(4.5f));
    }

    public void refresh() {
        if (mMap != null) {
            clear();
            drawTracks(mDb.getAllTracks());
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

    public void drawPoints(ArrayList<TrackPoint> points) {
        for (TrackPoint iPoint : points) {
            drawPoint(iPoint);
        }
    }

    public void drawTrack(Track track) {

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
    }

    public void drawTracks(ArrayList<Track> tracks) {
        for (int i = 0; i < tracks.size(); ++i) {
            Track track = tracks.get(i);
            drawTrack(track);

            if (i == tracks.size() - 1) {
                ArrayList<TrackPoint> points = track.getPoints();
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

                            gpsService.setGpsListener(new GpsListener() {
                                @Override
                                public void onLocationReceived(TrackPoint trackPoint) {
                                    drawPoint(trackPoint);
                                }
                            });

                            gpsService.startLocationUpdates();
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
            mStopRecording.hide();
            mStartRecording.show();

            refresh();

            GpsService.getInstance().stopLocationUpdates();
        }
    };

    public interface OnFragmentInteractionListener {
    }
}