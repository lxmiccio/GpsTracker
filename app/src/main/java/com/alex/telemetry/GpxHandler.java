package com.alex.telemetry;

import android.location.Location;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Alex on 26/02/2017.
 */

public class GpxHandler {

    static public void saveGpx(File path, String name, ArrayList<Location> locations) {
        ArrayList<Trkpt> trkpts = new ArrayList<>();
        for(Location iLocation : locations) {
            trkpts.add(new Trkpt(iLocation.getLatitude(), iLocation.getLongitude()));
        }

        Trkseg trkseg = new Trkseg(trkpts);
        Trk trk = new Trk(name, trkseg);
        Gpx gpx = new Gpx(trk);

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

        Gpx gpx = serializer.read(Gpx.class, source,false);
        return gpx;
    }

    static ArrayList<String> getTracksList(File path) {
        ArrayList<String> tracksList = new ArrayList<>();
        File[] files = new File(path.getAbsolutePath()).listFiles();
        if(files != null) {
            for(File iFile : files) {
                if(iFile.getAbsolutePath().endsWith(".gpx")) {
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
