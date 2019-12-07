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

    public int getLength() {
        int length = 0;
        if (mSessions.size() > 0) {
            length = mSessions.get(0).getLength();
        }
        return length;
    }

    @Override
    public String toString() {
        return "Track {" + '\'' + mName + '\'' + '\'' + mCreatedAt + '\'' + '}';
    }
}