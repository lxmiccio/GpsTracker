package com.gpstracker;

import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.Date;

public class Track {

    private long mId;
    private Date mCreatedAt;

    @Element(name = "name")
    private String mName;

    private ArrayList<Session> mSessions;

    public Track() {
        mId = -1;
        mCreatedAt = new Date();
        mSessions = new ArrayList<>();
    }

    public Track(String name) {
        mId = -1;
        mName = name;
        mCreatedAt = new Date();
        mSessions = new ArrayList<>();
    }

    public Track(long id, String name, Date createdAt) {
        mId = id;
        mName = name;
        mCreatedAt = createdAt;
        mSessions = new ArrayList<>();
    }

    public long getId() {
        return mId;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<Session> getSessions() {
        return mSessions;
    }

    public void addSession(Session session) {
        mSessions.add(session);
    }

    @Override
    public String toString() {
        return "Track {" + '\'' + mName + '\'' + '\'' + mCreatedAt + '\'' + '}';
    }
}