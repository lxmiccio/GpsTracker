package com.gpstracker;

import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

public class GpxHandler {

    static public void saveGpx(File path, Track track) {
        Gpx gpx = new Gpx(track);

        Log.d("GpxHandler", "path is " + path);
        Log.d("GpxHandler", "name is " + track.getName());
        Serializer serializer = new Persister();
        File result = new File(path, track.getName() + ".gpx");
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
}