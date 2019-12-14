package com.smarttracker.view.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.smarttracker.R;
import com.smarttracker.model.Session;
import com.smarttracker.model.TrackPoint;
import com.smarttracker.model.db.DatabaseHelper;
import com.smarttracker.services.GpsService;
import com.smarttracker.utils.SettingsHandler;

import java.util.ArrayList;

public abstract class GoogleMapsFragment extends Fragment implements OnMapReadyCallback {

    protected Polyline mRoute;
    protected PolylineOptions mRouteOptions;
    protected Marker mMarker;
    protected ArrayList<LatLng> mLatLngs;

    protected GoogleMap mMap;
    protected SupportMapFragment mMapFragment;

    protected long mStartingTime;
    protected Handler mTimerHandler;
    protected TextView mChronometer;

    protected TrackPoint mPreviousTrackPoint;
    protected int mTraveledDistance;

    protected DatabaseHelper mDb;
    protected GpsService mGpsService;

    protected int[] mColors = {
            Color.BLACK,
            Color.GRAY,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA
    };
    protected int mLastColor;

    public GoogleMapsFragment() {
        mMarker = null;
        mLatLngs = new ArrayList<>();
        mTimerHandler = new Handler();

        mDb = DatabaseHelper.getInstance();
        mGpsService = GpsService.getInstance();

        mLastColor = 0;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        setMapType(SettingsHandler.getMapType());
        setZoomControls(true);
    }

    public void setMapType(String type) {
        switch (type) {
            case "Normale":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satellitare":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Rilievo":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "Ibrida":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    protected void drawPoint(TrackPoint point) {
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

        if (mLatLngs.size() == 0) {
            mLatLngs.add(latLng);

            // Initialize PolylineOptions
            mRouteOptions = new PolylineOptions();
            mRouteOptions.color(Color.BLACK);
            mRouteOptions.visible(true);
            mRouteOptions.width(8);

            // Draw new location
            mRouteOptions.add(latLng);
        } else {
            mLatLngs.add(latLng);

            // Draw new location
            mRouteOptions.add(latLng);
        }

        // Add the polyline to the map
        mRoute = mMap.addPolyline(mRouteOptions);

        // Update user position
        drawMarker(point);
    }

    protected void drawMarker(TrackPoint point) {
        if (mMap != null) {
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());

            // Remove previous marker
            if (mMarker != null) {
                mMarker.remove();
                mMarker = null;
            }

            // Draw marker at user position
            Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_user);

            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            circleDrawable.setBounds(0, 0, 100, 100);
            circleDrawable.draw(canvas);

            // Add the marker to the map
            mMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng));
        }
    }

    protected void drawSession(Session session, int color) {
        // Initialize PolylineOptions
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(color);
        polylineOptions.visible(true);
        polylineOptions.width(8);

        ArrayList<TrackPoint> points = session.getPoints();
        for (int i = 0; i < points.size(); ++i) {
            LatLng latLng = new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude());
            polylineOptions.add(latLng);

            if (i == 0) {
                // Draw marker at starting position
                Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_starting_point);

                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                circleDrawable.setBounds(0, 0, 100, 100);
                circleDrawable.draw(canvas);

                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng).title(getString(R.string.starting_point_of) + " " + session.getName()));
            } else if (i == points.size() - 1) {
                // Draw marker at ending point
                Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_ending_point);

                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                circleDrawable.setBounds(0, 0, 100, 100);
                circleDrawable.draw(canvas);

                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(latLng).title(getString(R.string.ending_point_of) + " " + session.getName()));
            }
        }

        // Add the polyline to the map
        mMap.addPolyline(polylineOptions);

        if (points.size() > 0) {
            centerCamera(points.get(0));
        }
    }

    protected void drawSessions(ArrayList<Session> sessions) {
        for (int i = 0; i < sessions.size(); ++i) {
            Session session = sessions.get(i);
            drawSession(session, mColors[i % (mColors.length)]);

//            if (i == sessions.size() - 1) {
//                ArrayList<TrackPoint> points = session.getPoints();
//                if (points.size() > 0) {
//                    centerCamera(points.get(points.size() - 1));
//                }
//            }
        }
    }

    protected void centerCamera(TrackPoint point) {
        if (mMap != null) {
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));
        }
    }

    public void setZoomControls(boolean status) {
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(status);
        }
    }

    protected void resetMap() {
        if (mMap != null) {
            mMap.clear();
            mLatLngs.clear();

            mPreviousTrackPoint = null;
            mTraveledDistance = 0;

            // Update user position
            TrackPoint trackPoint = mGpsService.getLatestTrackPoint();
            if (trackPoint != null) {
                drawMarker(trackPoint);
            }
        }
    }

    protected void updateTraveledDistance(TrackPoint trackPoint) {
        if (mPreviousTrackPoint != null) {
            mTraveledDistance += trackPoint.distanceTo(mPreviousTrackPoint);
        } else {
            mTraveledDistance = 0;
        }
        mPreviousTrackPoint = trackPoint;
    }

    protected View.OnClickListener mCenterPositionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TrackPoint trackPoint = mGpsService.getLatestTrackPoint();
            if (trackPoint != null) {
                centerCamera(trackPoint);
            }
        }
    };

    protected Runnable mTimerTask = new Runnable() {
        public void run() {
            long unixTime = System.currentTimeMillis();
            long difference = unixTime - mStartingTime;
            difference /= 1000;

            String minutes = String.valueOf(difference / 60);
            minutes = String.format("%1$" + 2 + "s", minutes).replace(' ', '0');

            String seconds = String.valueOf(difference % 60);
            seconds = String.format("%1$" + 2 + "s", seconds).replace(' ', '0');

            String time = minutes + ":" + seconds;
            mChronometer.setText(time);

            if (mChronometer.getVisibility() == View.INVISIBLE) {
                mChronometer.setVisibility(View.VISIBLE);
            }

            mTimerHandler.postDelayed(mTimerTask, 1000);
        }
    };
}