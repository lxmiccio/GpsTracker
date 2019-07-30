package com.gpstracker;

import android.location.Location;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

@Root
public class Gpx {

    @Attribute(name = "version")
    private String version;

    @Attribute(name = "creator")
    private String creator;

    @Element(name = "trk")
    private Session mSession;

    public Gpx() {
        version = "1.1";
        creator = "FVDL";
        mSession = new Session();
    }

    public Gpx(Session session) {
        version = "1.1";
        creator = "FVDL";
        mSession = session;
    }

    public void appendPoint(TrackPoint point) {
        mSession.appendPoint(point);
    }

    public void appendPoint(Location location) {
        TrackPoint point = new TrackPoint(location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getTime());
        mSession.appendPoint(point);
    }

    public void save(File path, String name) {
        Serializer serializer = new Persister();
        File result = new File(path, name + ".gpx");
        try {
            serializer.write(this, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return mSession;
    }

    @Override
    public String toString() {
        return "Gpx {" + mSession + '}';
    }
}