package com.smarttracker.model;

import java.util.Date;

public class Track {

    private long mId;
    private String mName;
    private Date mCreatedAt;

    public Track(long id, String name, Date createdAt) {
        mId = id;
        mName = name;
        mCreatedAt = createdAt;
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

    @Override
    public String toString() {
        return "Track {" + '\'' + mName + '\'' + '\'' + mCreatedAt + '\'' + '}';
    }
}