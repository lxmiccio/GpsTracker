package com.gpstracker;

import java.util.Date;

public class Track {

    private long mId;
    private Date mStartingDate;
    private Date mEndingDate;

    public Track(long id, Date startingDate, Date endingDate) {
        mId = id;
        mStartingDate = startingDate;
        mEndingDate = endingDate;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getStartingDate() {
        return mStartingDate;
    }

    public Date getEndingDate() {
        return mEndingDate;
    }

    @Override
    public String toString() {
        return "Track {" + '\'' + mStartingDate + '\'' + '\'' + mEndingDate + '\'' + '}';
    }
}
