package com.gpstracker;

import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class GpxHandler {

    static public void saveGpx(File path, String name, ArrayList<Location> locations) {
        ArrayList<TrackPoint> trackPoints = new ArrayList<>();
        for (Location iLocation : locations) {
            double altitude = iLocation.getAltitude();
            float bearing = iLocation.getBearing();
            double latitude = iLocation.getLatitude();
            double longitude = iLocation.getLongitude();
            float speed = iLocation.getSpeed();
            long time = iLocation.getTime();

            trackPoints.add(new TrackPoint(altitude, bearing, latitude, longitude, speed, time));
            Log.d("SAVE", iLocation.getLatitude() + " " + iLocation.getLongitude());
        }

        TrackSegment trackSegment = new TrackSegment(trackPoints);
        Track track = new Track(name, trackSegment);
        Gpx gpx = new Gpx(track);

        Serializer serializer = new Persister();
        File result = new File(path, name + ".gpx");
        try {
            serializer.write(gpx, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Gpx loadGpx(File path, String name) throws Exception {
        Serializer serializer = new Persister();
        File source = new File(path, name + ".gpx");
        Log.d("SAVEEE", "fdfdg");


        Gpx gpx = serializer.read(Gpx.class, source, false);


        File file = new File(path, name + ".gpx");

//Read text from file
        StringBuilder text = new StringBuilder();
        Log.d("SAVEEE", "fdfdg");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            Log.d("SAVEEE", text.toString());
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }



        return gpx;
    }

    static ArrayList<String> getTracksList(File path) {
        ArrayList<String> tracksList = new ArrayList<>();
        File[] files = new File(path.getAbsolutePath()).listFiles();
        if (files != null) {
            for (File iFile : files) {
                if (iFile.getAbsolutePath().endsWith(".gpx")) {
                    String name = iFile.getAbsolutePath();
                    name = name.substring(name.lastIndexOf("/") + 1);
                    name = name.substring(0, name.length() - 4);
                    tracksList.add(name);
                }
            }
        }
        return tracksList;
    }
}