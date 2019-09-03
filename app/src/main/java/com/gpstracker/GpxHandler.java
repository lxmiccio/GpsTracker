package com.gpstracker;

import android.util.Log;

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

        //Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
        //Canvas canvas = new Canvas();
        //canvas.setBitmap(bitmap);

        //Paint paint = new Paint();
        //paint.setColor(Color.rgb(255, 155, 50));
        //paint.setStrokeWidth(10);

        //canvas.drawLine(0, 0,50, 50, paint);
        //canvas.drawLine(50, 50,100, 50, paint);

        //for (int i = 0; i < trackPoints.size() - 1; ++i) {
        //    TrackPoint point = trackPoints.get(i);
        //    TrackPoint nextPoint = trackPoints.get(i + 1);
        //    Log.d("GpsService", "x: " + point.getLatitude() + ", y: " + point.getLongitude());
        //    Log.d("GpsService", "next x: " + nextPoint.getLatitude() + ", next y: " + nextPoint.getLongitude());
        //    canvas.drawLine((float) point.getLatitude(), (float) point.getLongitude(),
        //            (float) nextPoint.getLatitude()+1, (float) nextPoint.getLongitude()+1, paint);
        //}
        //
        //
        //File image = new File(path, name + ".png");
        //
        //FileOutputStream fileOutputStream = null;
        //try {
        //    fileOutputStream = new FileOutputStream(image);
        //    // Use the compress method on the BitMap object to write image to the OutputStream
        //    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //} finally {
        //    try {
        //        fileOutputStream.close();
        //    } catch (IOException e) {
        //        e.printStackTrace();
        //    }
        //}
    }

    static public Gpx loadGpx(File path, String name) {
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