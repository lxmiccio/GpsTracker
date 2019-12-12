package com.smarttracker.services;

import com.smarttracker.model.TrackPoint;

public interface GpsListener {
    void onLocationReceived(TrackPoint trackPoint);
}
