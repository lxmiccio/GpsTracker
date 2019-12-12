package com.smarttracker.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Gpx {

    @Attribute(name = "version")
    private String version;

    @Attribute(name = "creator")
    private String creator;

    @Element(name = "trk")
    private Session mSession;

    public Gpx(Session session) {
        version = "1.1";
        creator = "SmartTracker";
        mSession = session;
    }

    public Session getSession() {
        return mSession;
    }

    @Override
    public String toString() {
        return "Gpx {" + mSession + '}';
    }
}