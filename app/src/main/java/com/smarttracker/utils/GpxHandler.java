package com.smarttracker.utils;

import android.util.Log;

import com.smarttracker.model.Gpx;
import com.smarttracker.model.Session;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;

public class GpxHandler {

    static public void saveGpx(File path, Session session) {
        Gpx gpx = new Gpx(session);

        Log.d("GpxHandler", "path is " + path);
        Log.d("GpxHandler", "name is " + session.getNameWithDate());
        
        Serializer serializer = new Persister();
        File result = new File(path, session.getNameWithDate() + ".gpx");
        try {
            serializer.write(gpx, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Gpx loadGpx(String path, String name) {
        File source = new File(path, name + ".gpx");
        Serializer serializer = new Persister();

        Gpx gpx = null;
        try {
            gpx = serializer.read(Gpx.class, source, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gpx;
    }

    static public ArrayList<String> getTracksList(String path) {
        ArrayList<String> tracksList = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getAbsolutePath().endsWith(".gpx")) {
                    String name = file.getAbsolutePath();
                    name = name.substring(name.lastIndexOf("/") + 1);
                    name = name.substring(0, name.length() - 4);
                    tracksList.add(name);
                }
            }
        }
        return tracksList;
    }
}