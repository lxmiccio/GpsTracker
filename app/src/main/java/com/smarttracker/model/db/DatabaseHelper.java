package com.smarttracker.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smarttracker.model.Session;
import com.smarttracker.model.Track;
import com.smarttracker.model.TrackPoint;
import com.smarttracker.utils.DateFormatUtils;
import com.smarttracker.view.activities.MainActivity;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "GpsTracker";

    // Database Version. Remember to change DATABASE_VERSION when adding or changing tables, otherwise database won't update
    private static final int DATABASE_VERSION = 194;

    // Table Names
    private static final String TABLE_TRACK = "tracks";
    private static final String TABLE_SESSION = "sessions";
    private static final String TABLE_COORDINATE = "coordinates";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_STARTED_AT = "started_at";
    private static final String KEY_FINISHED_AT = "finished_at";
    private static final String KEY_NAME = "name";
    private static final String KEY_LENGTH = "length";
    private static final String KEY_TRACK_ID = "track_id";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_TIME = "time";

    // Tracks table create statement
    private static final String CREATE_TABLE_TRACK = "CREATE TABLE "
            + TABLE_TRACK + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NAME + " VARCHAR,"
            + KEY_CREATED_AT + " DATETIME)";

    // Sessions table create statement
    private static final String CREATE_TABLE_SESSION = "CREATE TABLE "
            + TABLE_SESSION + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NAME + " VARCHAR,"
            + KEY_LENGTH + " NUMBER,"
            + KEY_STARTED_AT + " DATETIME,"
            + KEY_FINISHED_AT + " DATETIME,"
            + KEY_TRACK_ID + " INTEGER,"
            + " FOREIGN KEY (" + KEY_TRACK_ID + ") REFERENCES " + TABLE_TRACK + " (" + KEY_ID + ") ON DELETE CASCADE)";

    // Coordinates table create statement
    private static final String CREATE_TABLE_COORDINATE = "CREATE TABLE "
            + TABLE_COORDINATE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_ALTITUDE + " DOUBLE,"
            + KEY_BEARING + " FLOAT,"
            + KEY_LATITUDE + " DOUBLE,"
            + KEY_LONGITUDE + " DOUBLE,"
            + KEY_SPEED + " FLOAT,"
            + KEY_TIME + " NUMBER,"
            + KEY_CREATED_AT + " DATETIME,"
            + KEY_SESSION_ID + " INTEGER,"
            + " FOREIGN KEY (" + KEY_SESSION_ID + ") REFERENCES " + TABLE_SESSION + " (" + KEY_ID + ") ON DELETE CASCADE)";

    private static DatabaseHelper mInstance = null;

    protected DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(MainActivity.getContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_TRACK);
        db.execSQL(CREATE_TABLE_SESSION);
        db.execSQL(CREATE_TABLE_COORDINATE);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        // Enable Foreign Key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE);

        // Create new tables
        onCreate(db);
    }

    public Track createTrack(String name) {
        SQLiteDatabase db = getWritableDatabase();

        String currentDateTime = DateFormatUtils.getCurrentDateTime();

        ContentValues trackValues = new ContentValues();
        trackValues.put(KEY_NAME, name);
        trackValues.put(KEY_CREATED_AT, currentDateTime);

        // Insert a new Track
        long trackId = db.insert(TABLE_TRACK, null, trackValues);

        // Creates a new Track with the given id
        Date createdAt = DateFormatUtils.getDateTime(currentDateTime);
        Track track = new Track(trackId, name, createdAt);

        return track;
    }

    public Session createSession(Track track) {
        SQLiteDatabase db = getWritableDatabase();

        String currentDate = DateFormatUtils.getCurrentDateTime();
        String name = track.getName() + " " + currentDate;

        String currentDateTime = DateFormatUtils.getCurrentDateTime();

        ContentValues sessionValues = new ContentValues();
        sessionValues.put(KEY_NAME, name);
        sessionValues.put(KEY_LENGTH, 0);
        sessionValues.put(KEY_STARTED_AT, currentDateTime);
        sessionValues.put(KEY_FINISHED_AT, currentDateTime);
        sessionValues.put(KEY_TRACK_ID, track.getId());

        // Insert a new Session
        long sessionId = db.insert(TABLE_SESSION, null, sessionValues);

        // Creates a new Session with the given id
        Date createdAt = DateFormatUtils.getDateTime(currentDateTime);
        Session session = new Session(sessionId, name, createdAt, createdAt);

        return session;
    }

    public long createCoordinate(TrackPoint point, long sessionId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ALTITUDE, point.getAltitude());
        values.put(KEY_BEARING, point.getBearing());
        values.put(KEY_LATITUDE, point.getLatitude());
        values.put(KEY_LONGITUDE, point.getLongitude());
        values.put(KEY_SPEED, point.getSpeed());
        values.put(KEY_TIME, point.getTime());
        values.put(KEY_CREATED_AT, DateFormatUtils.getCurrentDateTime());
        values.put(KEY_SESSION_ID, sessionId);

        // Insert a new Coordinate
        long id = db.insert(TABLE_COORDINATE, null, values);

        return id;
    }

    public ArrayList<Track> getAllTracks() {
        ArrayList<Track> tracks = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TRACK;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all the Tracks
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                Date createdAt = DateFormatUtils.getDateTime(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT)));

                Track track = new Track(id, name, createdAt);
                tracks.add(track);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return tracks;
    }

    public ArrayList<Session> getAllSessions() {
        ArrayList<Session> sessions = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SESSION;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all the Sessions
        if (cursor.moveToFirst()) {
            do {
                Session session = parseSession(cursor);
                sessions.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return sessions;
    }

    public Session getSessionById(long sessionId) {
        Session session = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SESSION + " WHERE " + KEY_ID + " = " + sessionId;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all the Sessions
        if (cursor.moveToFirst()) {
            do {
                session = parseSession(cursor);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return session;
    }

    public ArrayList<Session> getSessionsByTrackId(long trackId) {
        ArrayList<Session> sessions = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SESSION + " WHERE " + KEY_TRACK_ID + " = " + trackId;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all the Sessions
        if (cursor.moveToFirst()) {
            do {
                Session session = parseSession(cursor);
                sessions.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return sessions;
    }

    public ArrayList<TrackPoint> getCoordinatesBySessionId(long id) {
        ArrayList<TrackPoint> coordinates = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COORDINATE + " WHERE " + KEY_SESSION_ID + " = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all the Coordinates
        if (cursor.moveToFirst()) {
            do {
                double altitude = cursor.getDouble(cursor.getColumnIndex(KEY_ALTITUDE));
                float bearing = cursor.getFloat(cursor.getColumnIndex(KEY_BEARING));
                double latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE));
                float speed = cursor.getFloat(cursor.getColumnIndex(KEY_SPEED));
                long time = cursor.getLong(cursor.getColumnIndex(KEY_TIME));

                TrackPoint coordinate = new TrackPoint(altitude, bearing, latitude, longitude, speed, time);
                coordinates.add(coordinate);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return coordinates;
    }

    public void updateSession(Session session) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE " + TABLE_SESSION + " SET " + KEY_LENGTH + " = " + session.getLength() + ", " + KEY_FINISHED_AT + " = '" + DateFormatUtils.getDateTime(session.getEndingDate()) + "' WHERE id = " + session.getId());
    }

    public void deleteTrack(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRACK + " WHERE " + KEY_ID + " = " + id);
        db.close();
    }

    public void deleteAllTracks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COORDINATE);
        db.execSQL("DELETE FROM " + TABLE_SESSION);
        db.execSQL("DELETE FROM " + TABLE_TRACK);
        db.close();
    }

    public void deleteSession(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Remove the Coordinates related to the Session to delete
        db.execSQL("DELETE FROM " + TABLE_COORDINATE + " WHERE " + KEY_SESSION_ID + " = " + id);

        // Retrieve the track id of the Session to delete
        String selectQuery = "SELECT * FROM " + TABLE_SESSION + " WHERE " + KEY_ID + " = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        long trackId = -1;
        if (cursor.moveToFirst()) {
            trackId = cursor.getLong(cursor.getColumnIndex(KEY_TRACK_ID));
        }

        // Delete the Session
        db.execSQL("DELETE FROM " + TABLE_SESSION + " WHERE " + KEY_ID + " = " + id);

        // Get the count of remaining Sessions for the Track
        selectQuery = "SELECT * FROM " + TABLE_SESSION + " WHERE " + KEY_TRACK_ID + " = " + trackId;
        cursor = db.rawQuery(selectQuery, null);

        // If the Track has no Sessions, delete it
        if (cursor.getCount() == 0) {
            db.execSQL("DELETE FROM " + TABLE_TRACK + " WHERE " + KEY_ID + " = " + trackId);
        }

        db.close();
    }

    public Track saveTrack(String name, Date date) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues trackValues = new ContentValues();
        trackValues.put(KEY_NAME, name);
        trackValues.put(KEY_CREATED_AT, DateFormatUtils.getDateTime(date));

        // Insert a new Track
        long trackId = db.insert(TABLE_TRACK, null, trackValues);

        Track newTrack = new Track(trackId, name, date);
        return newTrack;
    }

    public Session saveSession(Track track, Session session, long second) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues sessionValues = new ContentValues();
        sessionValues.put(KEY_NAME, session.getNameWithDate());
        sessionValues.put(KEY_LENGTH, session.getLength());
        sessionValues.put(KEY_STARTED_AT, DateFormatUtils.getDateTime(session.getStartingDate()));
        sessionValues.put(KEY_FINISHED_AT, DateFormatUtils.getDateTime(session.getEndingDate()));
        sessionValues.put(KEY_CREATED_AT, DateFormatUtils.getDateTime(session.getStartingDate()));
        sessionValues.put(KEY_TRACK_ID, track.getId());

        // Insert a new Session
        long sessionId = db.insert(TABLE_SESSION, null, sessionValues);

        String currentDateTime = DateFormatUtils.getCurrentDateTime();
        Date createdAt = DateFormatUtils.getDateTime(currentDateTime);
        Session newSession = new Session(sessionId, session.getNameWithDate(), createdAt, createdAt);

        return newSession;
    }

    public void saveCoordinate(Session session, TrackPoint point) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ALTITUDE, point.getAltitude());
        values.put(KEY_BEARING, point.getBearing());
        values.put(KEY_LATITUDE, point.getLatitude());
        values.put(KEY_LONGITUDE, point.getLongitude());
        values.put(KEY_SPEED, point.getSpeed());
        values.put(KEY_TIME, point.getTime());
        values.put(KEY_CREATED_AT, DateFormatUtils.getCurrentDateTime());
        values.put(KEY_SESSION_ID, session.getId());

        // Insert a new Coordinate
        long id = db.insert(TABLE_COORDINATE, null, values);
    }

    private Session parseSession(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
        String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
        Date startingDate = DateFormatUtils.getDateTime(cursor.getString(cursor.getColumnIndex(KEY_STARTED_AT)));
        Date endingDate = DateFormatUtils.getDateTime(cursor.getString(cursor.getColumnIndex(KEY_FINISHED_AT)));
        ArrayList<TrackPoint> points = getCoordinatesBySessionId(id);

        Session session = new Session(id, name, startingDate, endingDate);
        session.setPoints(points);

        return session;
    }
}
